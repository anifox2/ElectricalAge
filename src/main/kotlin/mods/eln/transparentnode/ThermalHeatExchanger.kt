package mods.eln.transparentnode

import mods.eln.Eln
import mods.eln.fluid.ElementSidedFluidHandler
import mods.eln.fluid.TankData
import mods.eln.i18n.I18N.tr
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Utils
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.NodeBase
import mods.eln.node.NodePeriodicPublishProcess
import mods.eln.node.transparent.EntityMetaTag
import mods.eln.node.transparent.TransparentNode
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoadInitializerByPowerDrop
import mods.eln.sim.nbt.NbtElectricalGateInput
import mods.eln.sim.nbt.NbtThermalLoad
import mods.eln.sim.process.destruct.ThermalLoadWatchDog
import mods.eln.sim.process.destruct.WorldExplosion
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.templates.FluidTank
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import org.lwjgl.opengl.GL11
import java.lang.Math.ceil
import java.lang.Math.min

class ThermalHeatExchangerDescriptor(
    name: String,
    val thermal: ThermalLoadInitializerByPowerDrop
): TransparentNodeDescriptor(
    name,
    ThermalHeatExchangerElement::class.java,
    ThermalHeatExchangerRender::class.java,
    EntityMetaTag.Fluid
) {

    val main = Eln.obj.getObj("thermal_heat_exchanger").getPart("Plane_Plane.001")

    init {
        thermal.setMaximalPower(16_000.0)
        voltageLevelColor = VoltageLevelColor.Thermal
    }

    fun draw() {
        GL11.glTranslated(-0.5, -0.5, 0.5)
        main.draw()
    }

    fun draw(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        poseStack.pushPose()
        poseStack.translate(-0.5, -0.5, 0.5)
        main.draw(poseStack, bufferSource, packedLight, packedOverlay)
        poseStack.popPose()
    }

    /*
    override fun handleRenderType(item: ItemStack, type: IItemRenderer.ItemRenderType) = true
    override fun shouldUseRenderHelper(type: IItemRenderer.ItemRenderType, item: ItemStack, helper: IItemRenderer.ItemRendererHelper) = true //type != IItemRenderer.ItemRenderType.INVENTORY
    override fun renderItem(type: IItemRenderer.ItemRenderType, item: ItemStack, vararg data: Any) =
        draw()//if (type == IItemRenderer.ItemRenderType.INVENTORY) super.renderItem(type, item, *data) else draw()
    */

    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(Component.literal(tr("Generates heat when supplied with ic2:hotcoolant")))
        list.add(Component.literal(tr("Ejects out ic2:coolant")))
        list.add(Component.literal(Utils.plotCelsius(tr("  Max. temperature: "), thermal.maximumTemperature)))
    }

    override fun mustHaveFloor() = false
    override fun mustHaveCeiling() = false
    override fun mustHaveWall() = false
    override fun mustHaveWallFrontInverse() = false
}

data class ThermalPairing(val input: Fluid, val output: Fluid, val joulesPerMb: Double, val maxMbInputPerTick: Int, val ratio: Double = 1.0, val reversible: Boolean = false, val minTemp: Double? = null, val maxTemp: Double? = null)

class ThermalHeatExchangerElement(
    transparentNode: TransparentNode,
    descriptor: TransparentNodeDescriptor
): TransparentNodeElement(transparentNode, descriptor) {

    companion object {
        val ic2hotcoolant: Fluid? = ForgeRegistries.FLUIDS.getValue(ResourceLocation("ic2hotcoolant"))
        val ic2coolant: Fluid? = ForgeRegistries.FLUIDS.getValue(ResourceLocation("ic2coolant"))
        val hotwater: Fluid? = ForgeRegistries.FLUIDS.getValue(ResourceLocation("hot_water"))
        val coldwater: Fluid? = ForgeRegistries.FLUIDS.getValue(ResourceLocation("cold_water"))
        val ic2hotwater: Fluid? = ForgeRegistries.FLUIDS.getValue(ResourceLocation("ic2hotwater"))
        // Use 'steam' but fall back on 'ic2steam'. Or, just die.
        val steam: Fluid? = ForgeRegistries.FLUIDS.getValue(ResourceLocation("steam")) ?: ForgeRegistries.FLUIDS.getValue(ResourceLocation("ic2steam"))
        val INPUT_SIDE = net.minecraft.core.Direction.DOWN
        val OUTPUT_SIDE = net.minecraft.core.Direction.UP

    }

    val thermalPairs = mutableListOf<ThermalPairing>()

    private var joulesPerTick = 0.0
    private var inputMbPerTick = 0
    private var outputMbPerTick = 0

    private val electricalControlLoad = NbtElectricalGateInput("control")
    private val thermalLoad = NbtThermalLoad("thermalLoad")
    val tankMap = mapOf(Pair(net.minecraft.core.Direction.DOWN, ElementSidedFluidHandler.TankData(FluidTank(1000), ArrayList())), Pair(net.minecraft.core.Direction.UP, ElementSidedFluidHandler.TankData(FluidTank(1000), ArrayList())))
    private val tank = ElementSidedFluidHandler(tankMap)
    private val thermalWatchdog = ThermalLoadWatchDog(thermalLoad)
    private val fluidRegulatorProcess = IProcess {
        val inputFluid = tank.getFluidType(INPUT_SIDE)
        var joulesPerMb = 0.0
        if (thermalPairs.isNotEmpty() && tank.getFluidAmount(INPUT_SIDE) > 0 && inputFluid != null) {

            thermalPairs.filter { it.input == inputFluid || (it.reversible && it.output == inputFluid) }.forEach {
                if (it.input == inputFluid) {
                    // Normal Forwards conversion
                    inputMbPerTick = moveFluidProcess(it.output, it.maxMbInputPerTick, it.ratio, it.minTemp, it.maxTemp)
                    outputMbPerTick = (inputMbPerTick * it.ratio).toInt()
                    joulesPerMb = it.joulesPerMb
                } else {
                    // Reversed conversion
                    inputMbPerTick = moveFluidProcess(it.input, it.maxMbInputPerTick, it.ratio, it.minTemp, it.maxTemp)
                    outputMbPerTick = (inputMbPerTick * it.ratio).toInt()
                    joulesPerMb = -it.joulesPerMb
                }
            }

        }
        joulesPerTick = outputMbPerTick * joulesPerMb
    }

    fun moveFluidProcess(outputFluid: Fluid, maxMbInputPerTick: Int, ratio: Double, minTemp: Double?, maxTemp: Double?): Int {
        // Check that we can put the amount into the output, then pull what we can from the input and put to the output

        val maxMbOutputPerTick = ceil(maxMbInputPerTick * ratio).toInt()
        //println("maxMbInputPerTick: $maxMbInputPerTick")
        //println("maxMbOutputPerTick: $maxMbOutputPerTick")
        val canMoveOutputMb = tank.getHandler(OUTPUT_SIDE)?.fill(FluidStack(outputFluid, maxMbOutputPerTick), FluidAction.SIMULATE) ?: 0
        //println("canMoveOutputMb: $canMoveOutputMb")
        var inTempRange = 1.0
        if (minTemp != null) {
            if (thermalLoad.temperatureCelsius < minTemp) {
                inTempRange = 0.0
            }
        }
        if (maxTemp != null) {
            if (thermalLoad.temperatureCelsius > maxTemp) {
                inTempRange = 0.0
            }
        }
        val shouldMoveOutputMb = min(maxMbOutputPerTick * electricalControlLoad.normalized, canMoveOutputMb.toDouble()) * inTempRange
        //println("shouldMoveOutputMb: $shouldMoveOutputMb")
        val predictedInputMb = ceil(shouldMoveOutputMb / ratio).toInt()
        //println("predictedInputMb: $predictedInputMb")
        if (predictedInputMb > 0) {
            val movedInputMb = tank.getHandler(INPUT_SIDE)?.drain(predictedInputMb, FluidAction.EXECUTE)?.amount?: 0
            tank.getHandler(OUTPUT_SIDE)?.fill(FluidStack(outputFluid, (movedInputMb * ratio).toInt()), FluidAction.EXECUTE)
            //println("movedInputMb: $movedInputMb")
            //println("movedOutputMb: $movedOutputMb")
            return movedInputMb
        }
        return 0
    }

    private val thermalRegulatorProcess = IProcess { time ->
        //Yes, it's magic number time. 1.25 is a rough estimate of the "what the fuck" measure I got from thermal power.
        val heatPower = joulesPerTick / (Eln.instance!!.thermalFrequency / Eln.instance!!.electricalFrequency) * 1.25 / time
        thermalLoad.movePowerTo(heatPower)
        //thermalLoad.PcTemp += heatPower
    }

    init {
        electricalLoadList.add(electricalControlLoad)
        thermalLoadList.add(thermalLoad)
        slowPreProcessList.add(fluidRegulatorProcess)
        thermalFastProcessList.add(thermalRegulatorProcess)
        slowProcessList.add(NodePeriodicPublishProcess(transparentNode, 2.0, 1.0))
        slowProcessList.add(thermalWatchdog)
        thermalWatchdog.setTemperatureLimits((descriptor as ThermalHeatExchangerDescriptor).thermal)
            .setDestroys(WorldExplosion(this).machineExplosion())

        if (ic2hotcoolant != null && ic2coolant != null) {
            //println("IC2 Coolant Enabled in Thermal Heat Exchanger")
            thermalPairs.add(ThermalPairing(ic2coolant, ic2hotcoolant, -1920.0 / 7.0, 9, 1.0, false, minTemp = 300.0))
            thermalPairs.add(ThermalPairing(ic2hotcoolant, ic2coolant, 1920.0 / 7.0, 9, 1.0, false))
        }

        if (ic2hotwater != null) {
            thermalPairs.add(ThermalPairing(ic2hotwater,Fluids.WATER,
                1 / 0.45 / 2, //Joules per mB
                36, //max mB input rate
                1.0, //ratio
                false,//reversible?
                minTemp = 26.85,
                maxTemp = 76.85))
        }

        if (steam != null) {
            //println("Steam Enabled in Thermal Heat Exchanger")
            thermalPairs.add(ThermalPairing(Fluids.WATER, steam, -1/0.45, 36,10.0, false, minTemp = 100.0))
        }

        thermalPairs.forEach {
            tank.addFluidWhitelist(INPUT_SIDE, it.input)
            tank.addFluidWhitelist(OUTPUT_SIDE, it.output)
            if (it.reversible) {
                tank.addFluidWhitelist(OUTPUT_SIDE, it.input)
                tank.addFluidWhitelist(INPUT_SIDE, it.output)
            }
        }
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU) = when {
        side == front && lrdu == LRDU.Down -> electricalControlLoad
        else -> null
    }

    override fun getThermalLoad(side: Direction, lrdu: LRDU) = when {
        side == front.inverse() && lrdu == LRDU.Down -> thermalLoad
        else -> null
    }

    override fun getConnectionMask(side: Direction, lrdu: LRDU) = when (lrdu) {
        LRDU.Down -> when (side) {
            front.inverse() -> NodeBase.maskThermal
            front -> NodeBase.maskElectricalGate
            else -> 0
        }
        else -> 0
    }

    // This would be thermalLoad.power but it's not accurate.
    override fun multiMeterString(side: Direction): String = Utils.plotPercent("Ctl:", electricalControlLoad.normalized)
    override fun thermoMeterString(side: Direction): String = Utils.plotCelsius("T:", thermalLoad.temperatureCelsius) + " " + Utils.plotPower(joulesPerTick * 20)

    override fun getWaila(): Map<String, String> = mutableMapOf(
        Pair(tr("Control"), Utils.plotPercent("", electricalControlLoad.normalized)),
        Pair(tr("input tank level"), tank.getFluidAmount(INPUT_SIDE).toString()),
        Pair(tr("output tank level"), tank.getFluidAmount(OUTPUT_SIDE).toString()),
        Pair(tr("input mB/t"), Utils.plotBuckets("", inputMbPerTick / 1000.0)),
        Pair(tr("output mB/t"), Utils.plotBuckets("", outputMbPerTick / 1000.0)),
        Pair(tr("joules per tick"), joulesPerTick.toString()),
        Pair(tr("thermal power"), Utils.plotPower(joulesPerTick * 20))
    )

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        tank.writeToNBT(nbt, "tank")
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        tank.readFromNBT(nbt, "tank")
    }

    override fun initialize() {
        (descriptor as ThermalHeatExchangerDescriptor).thermal.applyToThermalLoad(thermalLoad)
        connect()
    }

    override fun onBlockActivated(player: Player, side: Direction, vx: Float, vy: Float, vz: Float) = false

    /*
    override fun getFluidHandler(): net.minecraftforge.fluids.capability.IFluidHandler {
        return tank
    }
    */
}

class ThermalHeatExchangerRender(
    tileEntity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
): TransparentNodeElementRender(tileEntity, descriptor) {
    override fun draw() {
        front!!.glRotateXnRef()
        (transparentNodedescriptor as ThermalHeatExchangerDescriptor).draw()
    }

    override fun render(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        front?.rotateXnRef(poseStack)
        (transparentNodedescriptor as ThermalHeatExchangerDescriptor).draw(poseStack, bufferSource, packedLight, packedOverlay)
    }
}

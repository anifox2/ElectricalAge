package mods.eln.sixnode

import mods.eln.Eln
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.generic.GenericItemUsingDamageSlot
import mods.eln.gui.GuiContainerEln
import mods.eln.gui.GuiHelperContainer
import mods.eln.gui.IGuiObject
import mods.eln.gui.ISlotSkin
import mods.eln.i18n.I18N
import mods.eln.i18n.I18N.tr
import mods.eln.item.DielectricItem
import mods.eln.item.IConfigurable
import mods.eln.item.ItemMovingHelper
import mods.eln.gui.SlotFilter
import mods.eln.gui.ItemStackFilter
import mods.eln.gui.IItemStackFilter
import mods.eln.misc.*
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNode
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElement
import mods.eln.node.six.SixNodeElementInventory
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Capacitor
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sim.process.destruct.BipoleVoltageWatchdog
import mods.eln.sim.process.destruct.WorldExplosion
import mods.eln.wiki.Data
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemDisplayContext
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import org.lwjgl.opengl.GL11
import java.util.HashMap
import kotlin.math.abs
import kotlin.math.pow
import com.mojang.math.Axis

class PowerCapacitorSixDescriptor(name: String,
                                  obj: Obj3D,
                                  var serie: IFunction,
                                  var dischargeTao: Double) : SixNodeDescriptor(name, PowerCapacitorSixElement::class.java, PowerCapacitorSixRender::class.java) {
    private var CapacitorCore: Obj3D.Obj3DPart? = null
    private var CapacitorCables: Obj3D.Obj3DPart? = null
    private var Base: Obj3D.Obj3DPart? = null
    fun getCValue(cableCount: Int, nominalDielVoltage: Double): Double {
        if (cableCount == 0) return 1e-6
        val uTemp = nominalDielVoltage / Eln.LVU
        return serie.getValue((cableCount - 1) / uTemp / uTemp)
    }

    fun getCValue(inventory: Container): Double {
        val core = inventory.getItem(PowerCapacitorSixContainer.redId)
        val diel = inventory.getItem(PowerCapacitorSixContainer.dielectricId)
        return if (core.isEmpty || diel.isEmpty) getCValue(0, 0.0) else {
            getCValue(core.count, getUNominalValue(inventory))
        }
    }

    fun getUNominalValue(inventory: Container): Double {
        val diel = inventory.getItem(PowerCapacitorSixContainer.dielectricId)
        return if (diel.isEmpty) 10000.0 else {
            val desc = GenericItemUsingDamageDescriptor.getDescriptor(diel) as DielectricItem
            desc.uNominal * diel.count
        }
    }

    /*
    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addEnergy(newItemStack())
    }
    */

    fun draw() {
        if (null != Base) Base!!.draw()
        if (null != CapacitorCables) CapacitorCables!!.draw()
        if (null != CapacitorCore) CapacitorCore!!.draw()
    }

    override fun draw(poseStack: PoseStack, consumer: VertexConsumer, packedLight: Int, packedOverlay: Int, signal: Boolean) {
        Base?.draw(poseStack, consumer, packedLight, packedOverlay)
        CapacitorCables?.draw(poseStack, consumer, packedLight, packedOverlay)
        CapacitorCore?.draw(poseStack, consumer, packedLight, packedOverlay)
    }

    fun draw(poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        if (null != Base) Base!!.draw(poseStack, buffer, packedLight, packedOverlay)
        if (null != CapacitorCables) CapacitorCables!!.draw(poseStack, buffer, packedLight, packedOverlay)
        if (null != CapacitorCore) CapacitorCore!!.draw(poseStack, buffer, packedLight, packedOverlay)
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        level: net.minecraft.world.level.Level?,
        list: MutableList<Component>,
        flag: net.minecraft.world.item.TooltipFlag
    ) {
        super.appendHoverText(itemStack, level, list, flag)
        val descriptor = GenericItemUsingDamageDescriptor.getDescriptor(itemStack) as PowerCapacitorSixDescriptor
        // list.add(Component.literal(tr("Max Voltage") + ": " + Utils.plotVolt("V", descriptor.uNominal)))
        // list.add(Component.literal(tr("Capacitance") + ": " + Utils.plotValue(descriptor.getCValue(itemStack.count, descriptor.uNominal), "F")))
    }

    override fun getFrontFromPlace(side: Direction, player: Player): LRDU {
        return super.getFrontFromPlace(side, player)!!.left()
    }

    init {
        CapacitorCables = obj.getPart("CapacitorCables")
        CapacitorCore = obj.getPart("CapacitorCore")
        Base = obj.getPart("Base")
        voltageLevelColor = VoltageLevelColor.Neutral
    }
}

class PowerCapacitorSixElement(SixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElement(SixNode, side, descriptor), IConfigurable {
    var descriptor: PowerCapacitorSixDescriptor = descriptor as PowerCapacitorSixDescriptor
    override var inventory = SixNodeElementInventory(2, 64, this)
    var positiveLoad = NbtElectricalLoad("positiveLoad")
    var negativeLoad = NbtElectricalLoad("negativeLoad")
    var capacitor = Capacitor(positiveLoad, negativeLoad)
    var dischargeResistor = Resistor(positiveLoad, negativeLoad)
    var punkProcess: PunkProcess = PunkProcess()
    var voltageWatchdog = BipoleVoltageWatchdog(capacitor).setNominalVoltage(this.descriptor.getUNominalValue(this.inventory)).setDestroys(WorldExplosion(this).cableExplosion())
    var stdDischargeResistor = 0.0
    var fromNbt = false

    inner class PunkProcess : IProcess {
        var eLeft = 0.0
        var eLegaliseResistor = 0.0
        override fun process(time: Double) {
            if (eLeft <= 0) {
                eLeft = 0.0
                dischargeResistor.resistance = stdDischargeResistor
            } else {
                eLeft -= dischargeResistor.power * time
                dischargeResistor.resistance = eLegaliseResistor
            }
        }
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? {
        if (lrdu == front.right()) return positiveLoad
        return if (lrdu == front.left()) negativeLoad else null
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        return null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        if (lrdu == front.right()) return NodeBase.maskElectricalPower
        return if (lrdu == front.left()) NodeBase.maskElectricalPower else 0
    }

    override fun multiMeterString(): String {
        return Utils.plotVolt("U", abs(capacitor.voltage)) + Utils.plotAmpere("I", capacitor.current)
    }

    override fun getWaila(): Map<String, String> {
        val info: MutableMap<String, String> = HashMap()
        info[tr("Capacity")] = Utils.plotValue(capacitor.coulombs, "F")
        info[tr("Charge")] = Utils.plotEnergy("", capacitor.energy)
        if (Eln.wailaEasyMode) {
            info[tr("Voltage drop")] = Utils.plotVolt("", Math.abs(capacitor.voltage))
            info[tr("Current")] = Utils.plotAmpere("", Math.abs(capacitor.current))
        }
        return info
    }

    override fun thermoMeterString(): String {
        return ""
    }

    override fun initialize() {
        Eln.applySmallRs(positiveLoad)
        Eln.applySmallRs(negativeLoad)
        setupPhysical()
    }

    public override fun inventoryChanged() {
        super.inventoryChanged()
        setupPhysical()
    }

    fun setupPhysical() {
        val eOld = capacitor.energy
        capacitor.coulombs = descriptor.getCValue(inventory)
        stdDischargeResistor = descriptor.dischargeTao / capacitor.coulombs
        punkProcess.eLegaliseResistor = descriptor.getUNominalValue(inventory).pow(2.0) / 400
        if (fromNbt) {
            dischargeResistor.resistance = stdDischargeResistor
            fromNbt = false
        } else {
            val deltaE = capacitor.energy - eOld
            punkProcess.eLeft += deltaE
            if (deltaE < 0) {
                dischargeResistor.resistance = stdDischargeResistor
            } else {
                dischargeResistor.resistance = punkProcess.eLegaliseResistor
            }
        }
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putDouble("punkELeft", punkProcess.eLeft)
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        punkProcess.eLeft = nbt.getDouble("punkELeft")
        if (java.lang.Double.isNaN(punkProcess.eLeft)) punkProcess.eLeft = 0.0
        fromNbt = true
    }

    override fun hasGui(): Boolean {
        return true
    }

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return PowerCapacitorSixContainer(player, inventory)
    }

    override fun readConfigTool(compound: CompoundTag, invoker: Player) {
        if (compound.contains("capRedstoneAmt")) {
            val desired = compound.getInt("capRedstoneAmt")
            object : ItemMovingHelper() {
                override fun acceptsStack(stack: ItemStack): Boolean {
                    return stack.item === Items.REDSTONE
                }

                override fun newStackOfSize(size: Int): ItemStack {
                    return ItemStack(Items.REDSTONE, size)
                }
            }.move(invoker.inventory, inventory, PowerCapacitorSixContainer.redId, desired)
            reconnect()
        }
        if (compound.contains("capDielectricAmt")) {
            val desired = compound.getInt("capDielectricAmt")
            val dielectric = GenericItemUsingDamageDescriptor.getByName("Dielectric")
            object : ItemMovingHelper() {
                override fun acceptsStack(stack: ItemStack): Boolean {
                    return dielectric!!.checkSameItemStack(stack)
                }

                override fun newStackOfSize(items: Int): ItemStack {
                    return dielectric!!.newItemStack(items)
                }
            }.move(invoker.inventory, inventory, PowerCapacitorSixContainer.dielectricId, desired)
            reconnect()
        }
    }

    override fun writeConfigTool(compound: CompoundTag, invoker: Player) {
        var stack = inventory.getItem(PowerCapacitorSixContainer.redId)
        if (stack.isEmpty) {
            compound.putInt("capRedstoneAmt", 0)
        } else {
            compound.putInt("capRedstoneAmt", stack.count)
        }
        stack = inventory.getItem(PowerCapacitorSixContainer.dielectricId)
        if (stack.isEmpty) {
            compound.putInt("capDielectricAmt", 0)
        } else {
            compound.putInt("capDielectricAmt", stack.count)
        }
    }

    init {
        electricalLoadList.add(positiveLoad)
        electricalLoadList.add(negativeLoad)
        electricalComponentList.add(capacitor)
        electricalComponentList.add(dischargeResistor)
        electricalProcessList.add(punkProcess)
        slowProcessList.add(voltageWatchdog)
        positiveLoad.setAsMustBeFarFromInterSystem()
    }
}

class PowerCapacitorSixRender(tileEntity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElementRender(tileEntity, side, descriptor) {
    var descriptor: PowerCapacitorSixDescriptor = descriptor as PowerCapacitorSixDescriptor
    override var inventory = SixNodeElementInventory(2, 64, this)

    override fun draw() {
        val poseStack = currentPoseStack ?: return
        val buffer = currentBuffer ?: return
        val light = currentLight
        val overlay = currentOverlay
        
        poseStack.mulPose(Axis.XP.rotationDegrees(90f))
        front!!.rotatePoseOnX(poseStack)
        descriptor.draw(poseStack, buffer, light, overlay)
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen {
        return PowerCapacitorSixGui(player, inventory, this)
    }
}

class PowerCapacitorSixGui(player: Player, inventory: Container, var render: PowerCapacitorSixRender) : GuiContainerEln<PowerCapacitorSixContainer>(PowerCapacitorSixContainer(player, inventory), player.inventory, Component.literal("Power Capacitor")) {

    override fun guiObjectEvent(eventId: Int) {
        super.guiObjectEvent(eventId)
    }

    override fun renderBg(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        // super.renderBg(guiGraphics, f, x, y) // GuiContainerEln doesn't have renderBg implementation usually, or it's abstract
        helper!!.drawBackground(guiGraphics, x, y)
        helper!!.drawString(guiGraphics, 8, 8, tr("Capacity: %1\$F", Utils.plotValue(render.descriptor.getCValue(render.inventory))), -0x1000000)
        helper!!.drawString(guiGraphics, 8, 8 + 8 + 1, tr("Nominal voltage: %1\$V", Utils.plotValue(render.descriptor.getUNominalValue(render.inventory))), -0x1000000)
    }

    override fun newHelper(): GuiHelperContainer {
        return GuiHelperContainer(this, 176, 166 - 54, 8, 84 - 54)
    }
}

class PowerCapacitorSixContainer(player: Player, inventory: Container) : BasicContainer(player, inventory, arrayOf(
    SlotFilter(inventory, redId, 132, 8, 13, arrayOf(ItemStackFilter(Items.REDSTONE)),
        ISlotSkin.SlotSkin.medium, arrayOf(tr("Redstone slot"), tr("(Increases capacity)"))),
    GenericItemUsingDamageSlot(inventory, dielectricId, 132 + 20, 8, 20, arrayOf(DielectricItem::class.java),
        ISlotSkin.SlotSkin.medium, arrayOf(tr("Dielectric slot"), tr("(Increases maximum voltage)")))
)) {
    companion object {
        const val redId = 0
        const val dielectricId = 1
    }
}

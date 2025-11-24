package mods.eln.transparentnode

import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.cable.CableRenderType
import mods.eln.generic.GenericItemBlockUsingDamageDescriptor
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.generic.GenericItemUsingDamageSlot
import mods.eln.gui.GuiContainerEln
import mods.eln.gui.GuiHelperContainer
import mods.eln.gui.ISlotSkin.SlotSkin
import mods.eln.i18n.I18N.tr
import mods.eln.item.CaseItemDescriptor
import mods.eln.misc.ConfigCopyToolDescriptor
import mods.eln.cable.CopperCableDescriptor
import mods.eln.misc.FerromagneticCoreDescriptor
import mods.eln.item.IConfigurable
import mods.eln.misc.*
import mods.eln.node.NodeBase
import mods.eln.node.NodePeriodicPublishProcess
import mods.eln.node.transparent.TransparentNode
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeElementInventory
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.mna.process.TransformerInterSystemProcess
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sim.process.destruct.VoltageStateWatchDog
import mods.eln.sim.process.destruct.WorldExplosion
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.sound.LoopedSound
// import mods.eln.wiki.Data
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class DcDcDescriptor(name: String, objM: Obj3D, coreM: Obj3D, casingM: Obj3D, val minimalLoadToHum: Float):
    TransparentNodeDescriptor(name, DcDcElement::class.java, DcDcRender::class.java) {

    companion object {
        const val COIL_SCALE: Float = 4.0f
        const val COIL_SCALE_LIMIT: Int = 16
    }

    var main: Obj3D.Obj3DPart? = null
    var core: Obj3D.Obj3DPart? = null
    var coil: Obj3D.Obj3DPart? = null
    var casing: Obj3D.Obj3DPart? = null
    var casingLeftDoor: Obj3D.Obj3DPart? = null
    var casingRightDoor: Obj3D.Obj3DPart? = null

    init {
        main = objM.getPart("main")
        coil = objM.getPart("sbire")
        core = coreM.getPart("fero")
        casing = casingM.getPart("Case")
        casingLeftDoor = casingM.getPart("DoorL")
        casingRightDoor = casingM.getPart("DoorR")

        voltageLevelColor = VoltageLevelColor.Neutral
    }

    /*
    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        // Data.addWiring(newItemStack())
    }
    */

    // ...existing code...
    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        val lines = tr("Transforms an input voltage to\nan output voltage.")!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
        for (line in lines) {
            list.add(Component.literal(line))
        }
    }

    /*
    fun addRealismContext(list: MutableList<String>?): RealisticEnum {
        list?.add(tr("This DC/DC has unrealistic capacitance effects and can sink/source power that violates Newton's laws"))
        list?.add(tr("It is made this way to improve the performance of the simulator in large power networks"))
        return RealisticEnum.UNREALISTIC
    }
    */

    // ...existing code...

    internal fun draw(poseStack: com.mojang.blaze3d.vertex.PoseStack, buffer: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int, core: Obj3D.Obj3DPart?, priCableNbr: Int, secCableNbr: Int, hasCasing: Boolean, doorOpen: Float) {
        main?.draw(poseStack, buffer, packedLight, packedOverlay)
        core?.draw(poseStack, buffer, packedLight, packedOverlay)
        if (core != null) {
            if (priCableNbr != 0) {
                var scale = COIL_SCALE
                if (priCableNbr < COIL_SCALE_LIMIT) {
                    scale *= priCableNbr.toFloat() / COIL_SCALE_LIMIT
                }
                poseStack.pushPose()
                poseStack.scale(1f, scale * 2f / (priCableNbr + 1), 1f)
                poseStack.translate(0f, -0.125f * (priCableNbr - 1) / COIL_SCALE, 0f)
                for (idx in 0 until priCableNbr) {
                    coil?.draw(poseStack, buffer, packedLight, packedOverlay)
                    poseStack.translate(0f, 0.25f / COIL_SCALE, 0f)
                }
                poseStack.popPose()
            }
            if (secCableNbr != 0) {
                var scale = COIL_SCALE
                if (secCableNbr < COIL_SCALE_LIMIT) {
                    scale *= secCableNbr.toFloat() / COIL_SCALE_LIMIT
                }
                poseStack.pushPose()
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f))
                poseStack.scale(1f, scale * 2f / (secCableNbr + 1), 1f)
                poseStack.translate(0f, -0.125f * (secCableNbr - 1) / COIL_SCALE, 0f)
                for (idx in 0 until secCableNbr) {
                    coil?.draw(poseStack, buffer, packedLight, packedOverlay)
                    poseStack.translate(0f, 0.25f / COIL_SCALE, 0f)
                }
                poseStack.popPose()
            }
        }

        if (hasCasing) {
            casing?.draw(poseStack, buffer, packedLight, packedOverlay)
            casingLeftDoor?.draw(poseStack, buffer, packedLight, packedOverlay, -doorOpen * 90, 0f, 1f, 0f)
            casingRightDoor?.draw(poseStack, buffer, packedLight, packedOverlay, doorOpen * 90, 0f, 1f, 0f)
        }
    }
}

class DcDcElement(transparentNode: TransparentNode, descriptor: TransparentNodeDescriptor): TransparentNodeElement(transparentNode, descriptor), IConfigurable {
    val primaryLoad = NbtElectricalLoad("primaryLoad")
    val secondaryLoad = NbtElectricalLoad("secondaryLoad")

    val primaryVoltageSource = VoltageSource("primaryVoltageSource")
    val secondaryVoltageSource = VoltageSource("secondaryVoltageSource")

    val interSystemProcess = TransformerInterSystemProcess(primaryLoad, secondaryLoad, primaryVoltageSource, secondaryVoltageSource)

    override val inventory = TransparentNodeElementInventory(4, 64, this)

    var primaryMaxCurrent = 0.0
    var secondaryMaxCurrent = 0.0

    val primaryVoltageWatchdog = VoltageStateWatchDog(primaryLoad)
    val secondaryVoltageWatchdog = VoltageStateWatchDog(secondaryLoad)

    var populated = false

    var ratioControl = 1.0

    init {
        electricalLoadList.add(primaryLoad)
        electricalLoadList.add(secondaryLoad)
        electricalComponentList.add(primaryVoltageSource)
        electricalComponentList.add(secondaryVoltageSource)
        val exp = WorldExplosion(this).machineExplosion()
        slowProcessList.add(primaryVoltageWatchdog.setDestroys(exp))
        slowProcessList.add(secondaryVoltageWatchdog.setDestroys(exp))
        slowProcessList.add(NodePeriodicPublishProcess(node!!, 1.0, .5))
        slowProcessList.add(DcDcProcess(this))
    }

    override fun disconnectJob() {
        super.disconnectJob()
        Eln.simulator!!.mna.removeProcess(interSystemProcess)

    }

    override fun connectJob() {
        Eln.simulator!!.mna.addProcess(interSystemProcess)
        super.connectJob()
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU): ElectricalLoad? {
        if (lrdu != LRDU.Down) return null
        return when (side) {
            front.right() -> secondaryLoad
            front.left() -> primaryLoad
            else -> null
        }
    }

    override fun getThermalLoad(side: Direction, lrdu: LRDU): ThermalLoad? {
        return null
    }

    override fun getConnectionMask(side: Direction, lrdu: LRDU): Int {
        if (lrdu != LRDU.Down) return 0
        return when (side) {
            front.left() -> NodeBase.maskElectricalPower
            front.right() -> NodeBase.maskElectricalPower
            else -> 0
        }
    }

    override fun multiMeterString(side: Direction): String {
        if (side == front.left())
            return Utils.plotVolt("UP+:", primaryLoad.voltage) + Utils.plotAmpere("IP+:", -primaryLoad.current)
        return if (side == front.right())
            Utils.plotVolt("US+:", secondaryLoad.voltage) + Utils.plotAmpere("IS+:", -secondaryLoad.current)
        else
            Utils.plotVolt("UP+:", primaryLoad.voltage) + Utils.plotAmpere("IP+:", primaryVoltageSource.current) + Utils.plotVolt("  US+:", secondaryLoad.voltage) + Utils.plotAmpere("IS+:", secondaryVoltageSource.current)
    }

    override fun initialize() {
        primaryVoltageSource.connectTo(primaryLoad, null)
        secondaryVoltageSource.connectTo(secondaryLoad, null)
        electricalComponentList.add(primaryVoltageSource)
        electricalComponentList.add(secondaryVoltageSource)
        interSystemProcess.ratio = 1.0
        computeInventory()
        connect()
    }

    private fun computeInventory() {
        val primaryCable = inventory.getItem(DcDcContainer.primaryCableSlotId)
        val secondaryCable = inventory.getItem(DcDcContainer.secondaryCableSlotId)
        val core = inventory.getItem(DcDcContainer.ferromagneticSlotId)

        primaryVoltageWatchdog.setNominalVoltage(120_000.0)
        secondaryVoltageWatchdog.setNominalVoltage(120_000.0)

        primaryMaxCurrent = 5.0
        secondaryMaxCurrent = 5.0

        var coreFactor = 1.0
        if (core != null) {
            val coreDescriptor = GenericItemUsingDamageDescriptor.getDescriptor(core) as FerromagneticCoreDescriptor
            coreFactor = coreDescriptor.cableMultiplicator
        }

        if (primaryCable == null || core == null || primaryCable.count < 1) {
            primaryLoad.highImpedance()
            populated = false
        } else {
            primaryLoad.serialResistance = coreFactor * 0.01
        }

        if (secondaryCable == null || core == null || secondaryCable.count < 1) {
            secondaryLoad.highImpedance()
            populated = false
        } else {
            secondaryLoad.serialResistance = coreFactor * 0.01
        }

        populated = primaryCable != null && secondaryCable != null && primaryCable.count >= 1 && secondaryCable.count >= 1 && core != null

        ratioControl = if (populated) {
            secondaryCable!!.count.toDouble() / primaryCable!!.count.toDouble()
        } else {
            1.0
        }
    }

    override fun inventoryChange(inventory: Container?) {
        disconnect()
        computeInventory()
        connect()
        needPublish()
    }

    override fun onBlockActivated(player: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        return false
    }

    override fun hasGui(): Boolean {
        return true
    }

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return DcDcContainer(player, inventory)
    }

    override fun getLightOpacity(): Float {
        return 1.0f
    }

    override fun onGroundedChangedByClient() {
        super.onGroundedChangedByClient()
        computeInventory()
        reconnect()
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        try {
            if (inventory.getItem(0) == null)
                stream.writeByte(0)
            else
                stream.writeByte(inventory.getItem(0)!!.count)
            if (inventory.getItem(1) == null)
                stream.writeByte(0)
            else
                stream.writeByte(inventory.getItem(1)!!.count)
            Utils.serialiseItemStack(stream, inventory.getItem(DcDcContainer.ferromagneticSlotId))
            Utils.serialiseItemStack(stream, inventory.getItem(DcDcContainer.primaryCableSlotId))
            Utils.serialiseItemStack(stream, inventory.getItem(DcDcContainer.secondaryCableSlotId))
            node!!.lrduCubeMask.getTranslate(front.down()).serialize(stream)
            var load = 0f
            if (primaryMaxCurrent != 0.0 && secondaryMaxCurrent != 0.0) {
                load = Utils.limit(Math.max(primaryLoad.current / primaryMaxCurrent,
                    secondaryLoad.current / secondaryMaxCurrent), 0.0, 1.0).toFloat()
            }
            stream.writeFloat(load)
            stream.writeBoolean(inventory.getItem(3) != null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getWaila(): Map<String, String> {
        val info = HashMap<String, String>()
        info[tr("Ratio")] = Utils.plotValue(interSystemProcess.ratio)
        if (Eln.wailaEasyMode) {
            info[tr("Voltages")] = "\u00A7a" + Utils.plotVolt("", primaryLoad.voltage) + " " +
                "\u00A7e" + Utils.plotVolt("", secondaryLoad.voltage)
        }
        info[tr("Subsystem Matrix Size")] = Utils.renderDoubleSubsystemWaila(primaryLoad.subSystem, secondaryLoad.subSystem)
        return info
    }

    override fun readConfigTool(compound: CompoundTag, invoker: Player) {
        if (compound.contains("isolator")) {
            disconnect()
            reconnect()
            needPublish()
        }
        if (ConfigCopyToolDescriptor.readGenDescriptor(compound, "primary", inventory, DcDcContainer.primaryCableSlotId, invoker))
            inventoryChange(inventory)
        if (ConfigCopyToolDescriptor.readGenDescriptor(compound, "secondary", inventory, DcDcContainer.secondaryCableSlotId, invoker))
            inventoryChange(inventory)
        if (ConfigCopyToolDescriptor.readGenDescriptor(compound, "core", inventory, DcDcContainer.ferromagneticSlotId, invoker))
            inventoryChange(inventory)
    }

    override fun writeConfigTool(compound: CompoundTag, invoker: Player) {
        ConfigCopyToolDescriptor.writeGenDescriptor(compound, "primary", inventory.getItem(DcDcContainer.primaryCableSlotId))
        ConfigCopyToolDescriptor.writeGenDescriptor(compound, "secondary", inventory.getItem(DcDcContainer.secondaryCableSlotId))
        ConfigCopyToolDescriptor.writeGenDescriptor(compound, "core", inventory.getItem(DcDcContainer.ferromagneticSlotId))
    }
}

class DcDcProcess(val element: DcDcElement): IProcess {

    companion object {
        const val MAX_RATIO = 16.0
        const val MIN_RATIO = 1.0 / 16.0
    }

    override fun process(time: Double) {
        val ratio = when {
            element.ratioControl > MAX_RATIO -> MAX_RATIO
            element.ratioControl < MIN_RATIO -> MIN_RATIO
            else -> element.ratioControl
        }
        if (ratio.isFinite()) {
            element.interSystemProcess.ratio = ratio
        } else {
            element.interSystemProcess.ratio = 1.0
        }
    }
}

class DcDcRender(tileEntity: TransparentNodeBlockEntity, val descriptor: TransparentNodeDescriptor): TransparentNodeElementRender(tileEntity, descriptor) {

    override val inventory = TransparentNodeElementInventory(4, 64, this)

    val load = SlewLimiter(0.5f)

    var primaryStackSize: Byte = 0
    var secondaryStackSize: Byte = 0
    var priRender: CableRenderDescriptor? = null
    var secRender: CableRenderDescriptor? = null

    private var feroPart: Obj3D.Obj3DPart? = null
    private var hasCasing = false

    private val coordinate: Coordinate
    private val doorOpen: PhysicalInterpolator

    private val priConn = LRDUMask()
    private val secConn = LRDUMask()
    private val controlConn = LRDUMask()
    private val eConn = LRDUMask()
    private var cableRenderType: CableRenderType? = null

    init {
        addLoopedSound(object : LoopedSound("eln:Transformer", coordinate(), Attenuation.LINEAR) {
            override fun getVolume(): Float {
                return if (load.position > (descriptor as DcDcDescriptor).minimalLoadToHum)
                    0.1f * (load.position - descriptor.minimalLoadToHum) / (1 - descriptor.minimalLoadToHum)
                else
                    0f
            }
        })

        coordinate = Coordinate(tileEntity)
        doorOpen = PhysicalInterpolator(0.4f, 4.0f, 0.9f, 0.05f)
    }

    override fun render(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        poseStack.pushPose()
        front!!.rotateXnRef(poseStack)
        (descriptor as DcDcDescriptor).draw(poseStack, bufferSource, packedLight, packedOverlay, feroPart, primaryStackSize.toInt(), secondaryStackSize.toInt(), hasCasing, doorOpen.get())
        poseStack.popPose()
        cableRenderType = drawCable(poseStack, bufferSource, packedLight, packedOverlay, front!!.down(), priRender, priConn, cableRenderType)
        cableRenderType = drawCable(poseStack, bufferSource, packedLight, packedOverlay, front!!.down(), secRender, secConn, cableRenderType)
    }

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        try {
            primaryStackSize = stream.readByte()
            secondaryStackSize = stream.readByte()
            val feroStack = Utils.unserialiseItemStack(stream)
            if (feroStack != null) {
                val feroDesc = GenericItemUsingDamageDescriptor.getDescriptor(feroStack) as? FerromagneticCoreDescriptor
                if (feroDesc != null)
                    feroPart = feroDesc.feroPart
            }
            val priStack = Utils.unserialiseItemStack(stream)
            if (priStack != null) {
                val priDesc = GenericItemUsingDamageDescriptor.getDescriptor(priStack) as? ElectricalCableDescriptor
                if (priDesc != null)
                    priRender = priDesc.render
            }

            val secStack = Utils.unserialiseItemStack(stream)
            if (secStack != null) {
                val secDesc = GenericItemUsingDamageDescriptor.getDescriptor(secStack) as? ElectricalCableDescriptor
                if (secDesc != null)
                    secRender = secDesc.render
            }

            eConn.deserialize(stream)

            priConn.mask = 0
            secConn.mask = 0
            for (lrdu in LRDU.values()) {
                if(!eConn.get(lrdu)) continue
                if(front!!.down().applyLRDU(lrdu) == front!!.left()) {
                    priConn.set(lrdu, true)
                    continue
                }
                if(front!!.down().applyLRDU(lrdu) == front!!.right()) {
                    secConn.set(lrdu, true)
                    continue
                }
                controlConn.set(lrdu, true)
            }
            cableRenderType = null

            load.target = stream.readFloat()
            hasCasing = stream.readBoolean()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun getCableRenderSide(side: Direction, lrdu: LRDU): CableRenderDescriptor? {
        if (lrdu == LRDU.Down) {
            if (side == front!!.left()) return priRender
            if (side == front!!.right()) return secRender
            if (side == front && !grounded) return priRender
            if (side == front!!.back() && !grounded) return secRender
        }
        return null
    }

    override fun notifyNeighborSpawn() {
        super.notifyNeighborSpawn()
        cableRenderType = null
    }

    override fun refresh(deltaT: Float) {
        super.refresh(deltaT)
        load.step(deltaT)

        if (hasCasing) {
            if (!Utils.isPlayerAround(tileEntity.level!!, coordinate.moved(front!!).getAABB(0)))
                doorOpen.target = 0f
            else
                doorOpen.target = 1f
            doorOpen.step(deltaT)
        }
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen {
        return DcDcGui(player, inventory, this)
    }

    override fun draw() {
        // Empty implementation as we use render()
    }
}

class DcDcGui(player: Player, inventory: Container, val render: DcDcRender): GuiContainerEln<DcDcContainer>(DcDcContainer(player, inventory), player.inventory, Component.literal("DC/DC Converter")) {
    override fun newHelper(): GuiHelperContainer {
        return GuiHelperContainer(this, 176, 194 - 33 + 20, 8, 84 + 194 - 166 - 33 + 20, "transformer.png")
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        helper?.render(guiGraphics, leftPos, topPos)
    }
}

class DcDcContainer(player: Player, inventory: Container) : BasicContainer(player, inventory,
    arrayOf(
        GenericItemUsingDamageSlot(inventory, primaryCableSlotId, 58, 30, 16,
            arrayOf<Class<*>>(CopperCableDescriptor::class.java),
            SlotSkin.medium, arrayOf(tr("Copper cable slot"))),
        GenericItemUsingDamageSlot(inventory, secondaryCableSlotId, 100, 30, 16,
            arrayOf<Class<*>>(CopperCableDescriptor::class.java),
            SlotSkin.medium, arrayOf(tr("Copper cable slot"))),
        GenericItemUsingDamageSlot(inventory, ferromagneticSlotId, 58 + (100 - 58) / 2, 30, 1,
            arrayOf<Class<*>>(FerromagneticCoreDescriptor::class.java),
            SlotSkin.medium, arrayOf(tr("Ferromagnetic core slot"))),
        GenericItemUsingDamageSlot(inventory, CasingSlotId, 130, 74, 1,
            arrayOf<Class<*>>(CaseItemDescriptor::class.java),
            SlotSkin.medium, arrayOf(tr("Casing slot")))))
    {
    companion object {
        const val primaryCableSlotId = 0
        const val secondaryCableSlotId = 1
        const val ferromagneticSlotId = 2
        const val CasingSlotId = 3
    }
}

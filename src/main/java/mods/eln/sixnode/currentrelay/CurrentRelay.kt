package mods.eln.sixnode.currentrelay

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.item.IConfigurable
import mods.eln.misc.*
import mods.eln.misc.LRDU.Companion.fromInt
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.misc.Utils.plotAmpere
import mods.eln.misc.Utils.plotVolt
import mods.eln.misc.Utils.renderSubSystemWaila
import mods.eln.misc.UtilsClient.disableCulling
import mods.eln.misc.UtilsClient.enableCulling
import mods.eln.node.NodeBase
import mods.eln.node.six.*
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.NodeElectricalGateInputHysteresisProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.nbt.NbtElectricalGateInput
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sim.nbt.NbtThermalLoad
import mods.eln.sim.process.destruct.ThermalLoadWatchDog
import mods.eln.sim.process.destruct.VoltageStateWatchDog
import mods.eln.sim.process.destruct.WorldExplosion
import mods.eln.sim.process.heater.ElectricalLoadHeatThermalLoad
import mods.eln.sixnode.currentcable.CurrentCableDescriptor
import mods.eln.sixnode.electricalrelay.ElectricalRelayElement
import mods.eln.sound.SoundCommand
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

// Temporary compatibility shim: provide a default nodeMask for current-cable descriptors.
// TODO: Replace with the real mask from CurrentCableDescriptor once that class is fully ported.
val CurrentCableDescriptor.nodeMask: Int
    get() = NodeBase.maskElectricalInputGate

class CurrentRelayDescriptor(
    name: String?,
    override var obj: Obj3D?,
    val cable: CurrentCableDescriptor
) : SixNodeDescriptor(name, CurrentRelayElement::class.java, CurrentRelayRender::class.java) {
    var speed: Float = 0f

    private val relay1: Obj3DPart = obj!!.getPart("relay1")
    private val relay0: Obj3DPart = obj!!.getPart("relay0")
    private val main: Obj3DPart = obj!!.getPart("main")
    private val backplate: Obj3DPart = obj!!.getPart("backplate")

    private var r0rOff = 0f
    private var r0rOn = 0f
    private var r1rOff = 0f
    private var r1rOn = 0f

    var thermalRp = 1.0
    var thermalC = 1.0
    var thermalRs = 1.0

    private val electricalRs = 0.01

    init {
        r0rOff = relay0.getFloat("rOff")
        r0rOn = relay0.getFloat("rOn")
        speed = relay0.getFloat("speed")
        r1rOff = relay1.getFloat("rOff")
        r1rOn = relay1.getFloat("rOn")
        this.voltageLevelColor = VoltageLevelColor.Neutral
    }

    fun setPhysicalConstantLikeNormalCable(
        electricalMaximalCurrent: Double
    ) {
        val thermalMaximalPowerDissipated = electricalRs * electricalMaximalCurrent * electricalMaximalCurrent * 2
        thermalC = thermalMaximalPowerDissipated * Eln.cableHeatingTime / Eln.cableWarmLimit
        thermalRp = Eln.cableWarmLimit / thermalMaximalPowerDissipated
        thermalRs = 0.25 / thermalC
    }

    fun applyTo(load: ElectricalLoad) {
        cable.applyTo(load)
    }

    fun applyTo(load: Resistor) {
        cable.applyTo(load)
    }

    /**
     * Tooltip helper for the relay item. Uses modern Component-based tooltips.
     *
     * NOTE: This intentionally does *not* use @Override so it doesn't depend
     * on the exact signature defined in SixNodeDescriptor/Item wrappers.
     * You can wire this up from your Item's appendHoverText implementation.
     */
    fun addInformation(
        itemStack: ItemStack?,
        level: Level?,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        // Basic description
        tr(
            "A relay is an electrical\n" +
                "contact that conducts\n" +
                "current when a signal\n" +
                "voltage is applied."
        )
            .split("\n")
            .filter { it.isNotEmpty() }
            .map { Component.literal(it) }
            .forEach(tooltip::add)

        // Schmitt trigger behaviour
        tr("The relay's input behaves\nlike a Schmitt Trigger.")
            .split("\n")
            .filter { it.isNotEmpty() }
            .map { Component.literal(it) }
            .forEach(tooltip::add)
    }



    fun draw(poseStack: PoseStack, consumer: VertexConsumer, packedLight: Int, packedOverlay: Int, factor: Float) {
        poseStack.pushPose()
        poseStack.scale(0.5f, 0.5f, 0.5f)

        main.draw(poseStack, consumer, packedLight, packedOverlay)

        relay0.draw(poseStack, consumer, packedLight, packedOverlay, factor * (r0rOn - r0rOff) + r0rOff, 0f, 0f, 1f)
        relay1.draw(poseStack, consumer, packedLight, packedOverlay, factor * (r1rOn - r1rOff) + r1rOff, 0f, 0f, 1f)

        val r = (voltageLevelColor.getRed() * 255).toInt()
        val g = (voltageLevelColor.getGreen() * 255).toInt()
        val b = (voltageLevelColor.getBlue() * 255).toInt()
        val a = 255

        backplate.drawColored(poseStack, consumer, packedLight, packedOverlay, r, g, b, a)

        poseStack.popPose()
    }

    override fun getFrontFromPlace(side: Direction, player: Player): LRDU {
        // Keep the original behaviour (front then rotated left), but use modern Player
        return super.getFrontFromPlace(side, player)!!.left()
    }
}

class CurrentRelayElement(
    sixNode: SixNode,
    side: Direction,
    descriptor: SixNodeDescriptor
) : SixNodeElement(sixNode, side, descriptor), IConfigurable {

    private val currentRelayDescriptor = descriptor as CurrentRelayDescriptor

    var aLoad = NbtElectricalLoad("aLoad")
    var bLoad = NbtElectricalLoad("bLoad")
    var thermalLoad = NbtThermalLoad("thermalLoad")
    private var switchResistor = Resistor(aLoad, bLoad)
    var gate = NbtElectricalGateInput("gate")
    private var gateProcess = CurrentRelayGateProcess(this, "GP", gate)

    private var voltageWatchDogA = VoltageStateWatchDog(aLoad)
    private var voltageWatchDogB = VoltageStateWatchDog(bLoad)
    private var thermalWatchdog = ThermalLoadWatchDog(thermalLoad)

    var switchState = false
        set(value) {
            if (value == switchState) return
            field = value
            refreshSwitchResistor()
            play(SoundCommand("random.click").mulVolume(0.1f, 2.0f).smallRange())
            needPublish()
        }

    var defaultOutput = false

    val cableDescriptor = currentRelayDescriptor.cable

    init {
        configThermalLoad(thermalLoad)

        electricalLoadList.add(aLoad)
        electricalLoadList.add(bLoad)
        electricalComponentList.add(switchResistor)
        electricalProcessList.add(gateProcess)
        electricalLoadList.add(gate)
        thermalLoadList.add(thermalLoad)

        electricalComponentList.add(Resistor(bLoad, null).pullDown())
        electricalComponentList.add(Resistor(aLoad, null).pullDown())

        slowProcessList.add(voltageWatchDogA)
        slowProcessList.add(voltageWatchDogB)

        voltageWatchDogA.setNominalVoltage(cableDescriptor.electricalNominalVoltage)
        voltageWatchDogB.setNominalVoltage(cableDescriptor.electricalNominalVoltage)

        val heater = ElectricalLoadHeatThermalLoad(aLoad, thermalLoad)
        thermalSlowProcessList.add(heater)

        thermalLoad.setAsSlow()

        slowProcessList.add(thermalWatchdog)
        thermalWatchdog
            .setTemperatureLimits(Eln.cableWarmLimit, -10.0)
            .setDestroys(WorldExplosion(this).cableExplosion())
    }

    @Suppress("UNUSED_PARAMETER")
    fun canBePlacedOnSide(side: Direction?, type: Int): Boolean {
        return true
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        val value = nbt.getByte("front")
        front = fromInt(value.toInt() shr 0 and 0x3)
        switchState = nbt.getBoolean("switchState")
        defaultOutput = nbt.getBoolean("defaultOutput")
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putByte("front", (front.toInt() shl 0).toByte())
        nbt.putBoolean("switchState", switchState)
        nbt.putBoolean("defaultOutput", defaultOutput)
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? {
        if (front.left() === lrdu) return aLoad
        if (front.right() === lrdu) return bLoad
        return if (front === lrdu) gate else null
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        if (front.left() === lrdu) return thermalLoad
        if (front.right() === lrdu) return thermalLoad
        return null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        if (front.left() === lrdu) return cableDescriptor.nodeMask
        if (front.right() === lrdu) return cableDescriptor.nodeMask
        return if (front === lrdu) NodeBase.maskElectricalInputGate else 0
    }

    override fun multiMeterString(): String {
        return plotVolt("Ua:", aLoad.voltage) + plotVolt("Ub:", bLoad.voltage) + plotAmpere("I:", aLoad.current)
    }

    override fun getWaila(): Map<String, String> {
        val info: MutableMap<String, String> = HashMap()
        info[tr("Position")] = if (switchState) tr("Closed") else tr("Open")
        info[tr("Current")] = plotAmpere("", aLoad.current)
        info[tr("Temperature")] = Utils.plotCelsius("", thermalLoad.temperatureCelsius)
        if (Eln.wailaEasyMode) {
            info[tr("Default position")] = if (defaultOutput) tr("Closed") else tr("Open")
            info[tr("Voltages")] =
                plotVolt("", aLoad.voltage) + plotVolt(" ", bLoad.voltage)
        }
        info[tr("Subsystem Matrix Size")] = renderSubSystemWaila(switchResistor.subSystem)
        return info
    }

    override fun thermoMeterString(): String {
        return Utils.plotCelsius("T", thermalLoad.temperatureCelsius)
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        try {
            stream.writeBoolean(switchState)
            stream.writeBoolean(defaultOutput)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun refreshSwitchResistor() {
        if (!switchState) {
            switchResistor.ultraImpedance()
        } else {
            currentRelayDescriptor.applyTo(switchResistor)
        }
    }

    override fun initialize() {
        computeElectricalLoad()
        refreshSwitchResistor()
    }

    override fun inventoryChanged() {
        computeElectricalLoad()
    }

    fun computeElectricalLoad() {
        currentRelayDescriptor.applyTo(aLoad)
        currentRelayDescriptor.applyTo(bLoad)
        refreshSwitchResistor()
    }

    private fun configThermalLoad(thermalLoad: ThermalLoad) {
        thermalLoad.Rs = currentRelayDescriptor.thermalRs
        thermalLoad.heatCapacity = currentRelayDescriptor.thermalC
        thermalLoad.Rp = currentRelayDescriptor.thermalRp
    }

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        try {
            when (stream.readByte()) {
                ElectricalRelayElement.toogleOutputDefaultId -> {
                    defaultOutput = !defaultOutput
                    needPublish()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun hasGui(): Boolean {
        return true
    }

    override fun readConfigTool(compound: CompoundTag, invoker: Player) {
        if (compound.contains("nc")) {
            defaultOutput = compound.getBoolean("nc")
        }
    }

    override fun writeConfigTool(compound: CompoundTag, invoker: Player) {
        compound.putBoolean("nc", defaultOutput)
    }
}

class CurrentRelayGateProcess(
    val element: CurrentRelayElement,
    name: String?,
    gateProcess: NbtElectricalGateInput
) : NodeElectricalGateInputHysteresisProcess(name, gateProcess) {
    override fun setOutput(value: Boolean) {
        element.switchState = value xor element.defaultOutput
    }
}

class CurrentRelayGui(val render: CurrentRelayRender) : Screen(Component.literal("Current Relay")) {

    override fun init() {
        super.init()
        // Simple button to toggle the relay's default output state.
        val buttonWidth = 120
        val buttonHeight = 20
        val x = this.width / 2 - buttonWidth / 2
        val y = this.height / 2 - buttonHeight / 2

        addRenderableWidget(
            Button.builder(Component.literal(tr("Toggle switch"))) {
                render.clientToggleDefaultOutput()
            }.bounds(x, y, buttonWidth, buttonHeight).build()
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }
}

class CurrentRelayRender(
    blockEntity: SixNodeEntity,
    side: Direction,
    descriptor: SixNodeDescriptor
) : SixNodeElementRender(blockEntity, side, descriptor) {

    private val currentRelayDescriptor = descriptor as CurrentRelayDescriptor

    val interpolator = RcInterpolator(currentRelayDescriptor.speed)

    var boot = true
    var switchState = true
    var defaultOutput = true

    override fun draw() {
        super.draw()
        val poseStack = currentPoseStack ?: return
        val buffer = currentBuffer ?: return
        val light = currentLight
        val overlay = currentOverlay

        drawSignalPin(poseStack, buffer, light, overlay, floatArrayOf(2.5f, 2.5f, 2.5f, 2.5f))
        
        val consumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.solid())
        
        poseStack.pushPose()
        front!!.rotatePoseOnX(poseStack)
        currentRelayDescriptor.draw(poseStack, consumer, light, overlay, interpolator.get())
        poseStack.popPose()
    }

    override fun refresh(deltaT: Float) {
        interpolator.step(deltaT)
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        try {
            switchState = stream.readBoolean()
            defaultOutput = stream.readBoolean()
            interpolator.target = if (switchState) 1f else 0f
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (boot) {
            interpolator.setValueFromTarget()
        }
        boot = false
    }

    fun clientToggleDefaultOutput() {
        clientSend(ElectricalRelayElement.toogleOutputDefaultId.toInt())
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen {
        return CurrentRelayGui(this)
    }

    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? {
        if (lrdu === front) return Eln.instance!!.signalCableDescriptor?.render
        val render = currentRelayDescriptor.cable?.render
        return if (lrdu === front!!.left() || lrdu === front!!.right()) render else null
    }
}

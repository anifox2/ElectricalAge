package mods.eln.sixnode

import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.gui.GuiHelperContainer
import mods.eln.gui.ScreenEln
import mods.eln.gui.GuiTextFieldEln
import mods.eln.i18n.I18N.tr
import mods.eln.item.IConfigurable
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Obj3D
import mods.eln.misc.Utils
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNode
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElement
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.CurrentSource
import mods.eln.sim.nbt.NbtElectricalLoad
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class PowerSourceDescriptor(name: String, obj: Obj3D) : SixNodeDescriptor(name, PowerSourceElement::class.java, PowerSourceRender::class.java) {

    private var main: Obj3D.Obj3DPart = obj.getPart("main")
    fun draw() {
        main.draw()
    }

    override fun appendHoverText(itemStack: ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        val lines = tr("Provides an ideal power source\nwithout energy or power limitation.").split("\n")
        for (line in lines) {
            list.add(Component.literal(line))
        }
        list.add(Component.literal(""))
        list.add(Component.literal(tr("Internal resistance: %1$\u2126", Utils.plotValue(Eln.lowVoltageCableDescriptor!!.electricalRs))))
        list.add(Component.literal(""))
        list.add(Component.literal(tr("Creative block.")))
    }

    override fun canBePlacedOnSide(player: Player?, side: Direction) = true

    init {
        voltageLevelColor = VoltageLevelColor.Neutral
    }
}

class PowerSourceElement(_sixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElement(_sixNode, side, descriptor), IConfigurable {
    var electricalLoad = NbtElectricalLoad("electricalLoad")
    var currentSource = CurrentSource("currSrc", electricalLoad, null)

    init {
        electricalLoadList.add(electricalLoad)
        electricalComponentList.add(currentSource)
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        currentSource.current = nbt.getDouble("current")
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putDouble("current", currentSource.current)
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad {
        return electricalLoad
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        return null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        return NodeBase.maskElectricalPower
    }

    override fun multiMeterString(): String {
        return Utils.plotUIP(electricalLoad.voltage, currentSource.current)
    }

    override fun getWaila(): Map<String, String> {
        val info: MutableMap<String, String> = HashMap()
        info[tr("Voltage")] = Utils.plotVolt("", electricalLoad.voltage)
        info[tr("Current")] = Utils.plotAmpere("", electricalLoad.current)
        if (Eln.wailaEasyMode) {
            info[tr("Power")] = Utils.plotPower("", electricalLoad.voltage * electricalLoad.current)
        }
        return info
    }

    override fun thermoMeterString(): String {
        return ""
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        try {
            stream.writeDouble(currentSource.current)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        try {
            currentSource.current = stream.readDouble()
            needPublish()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun initialize() {
        Eln.applySmallRs(electricalLoad)
    }

    override fun onBlockActivated(entityPlayer: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        return onBlockActivatedRotate(entityPlayer)
    }

    override fun hasGui(): Boolean {
        return true
    }

    override fun readConfigTool(compound: CompoundTag, invoker: Player) {
        if (compound.contains("current")) {
            currentSource.current = compound.getDouble("current")
            needPublish()
        }
    }

    override fun writeConfigTool(compound: CompoundTag, invoker: Player) {
        compound.putDouble("current", currentSource.current)
    }
}

class PowerSourceGui(var render: PowerSourceRender) : ScreenEln(), GuiTextFieldEln.GuiTextFieldElnObserver {
    var current: GuiTextFieldEln? = null
    override fun newHelper(): GuiHelperContainer {
        return GuiHelperContainer(this, 50 + 12, 12 + 12)
    }

    override fun initGui() {
        super.initGui()
        current = newGuiTextField(6, 6, 50)
        current!!.value = render.current.toString()
        current!!.setObserver(this)
        current!!.setComment(listOf(tr("Power sourced")))
    }

    override fun textFieldNewValue(textField: GuiTextFieldEln, value: String) {

        val newCurrent = current!!.value.toDoubleOrNull()?: 0.0

        try {
            val bos = ByteArrayOutputStream()
            val stream = DataOutputStream(bos)
            render.preparePacketForServer(stream)
            stream.writeDouble(newCurrent)
            render.sendPacketToServer(bos)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

class PowerSourceRender(tileEntity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElementRender(tileEntity, side, descriptor) {
    var descriptor: PowerSourceDescriptor = descriptor as PowerSourceDescriptor
    var voltage = 0.0
    @JvmField
    var current = 0.0
    override fun draw() {
        super.draw()
        front!!.glRotateOnX()
        descriptor.draw()
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        try {
            current = stream.readDouble()
            needRedrawCable()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen {
        return PowerSourceGui(this)
    }

    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? {
        return Eln.veryHighVoltageCableDescriptor!!.render
    }
}

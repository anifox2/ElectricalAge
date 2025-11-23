package mods.eln.sixnode

import mods.eln.Eln
import mods.eln.cable.CableRender
import mods.eln.cable.CableRenderDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.misc.*
import mods.eln.node.NodeBase
import mods.eln.node.six.*
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sim.nbt.NbtThermalLoad
import mods.eln.sixnode.genericcable.GenericCableDescriptor
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import org.lwjgl.opengl.GL11
import java.util.HashMap

class PortableNaNDescriptor(name: String, renderIn: CableRenderDescriptor): GenericCableDescriptor(name, PortableNaNElement::class.java, PortableNaNRender::class.java) {

    init {
        this.render = renderIn
    }

    override fun applyTo(electricalLoad: ElectricalLoad, rsFactor: Double) {
        electricalLoad.serialResistance = Double.NaN
    }

    override fun applyTo(electricalLoad: ElectricalLoad) {
        electricalLoad.serialResistance = Double.NaN
    }

    override fun applyTo(resistor: Resistor) {
        resistor.resistance = Double.NaN
    }

    override fun applyTo(resistor: Resistor, factor: Double) {
        resistor.resistance = Double.NaN
    }

    override fun applyTo(thermalLoad: ThermalLoad) = thermalLoad.set(Double.NaN, Double.NaN, Double.NaN)

    override fun appendHoverText(itemStack: ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)

        list.add(Component.literal(tr("Nominal Ratings:")))
        list.add(Component.literal("  " + tr("Voltage: Yes")))
        list.add(Component.literal("  " + tr("Current: No")))
        list.add(Component.literal("  " + tr("Serial Resistance: OK Ω")))
        list.add(Component.literal(tr("A debugging feature that throws NaN (Not a Number) anywhere it can in the simulator to find bugs")))
    }

    override fun getNodeMask(): Int {
        return NodeBase.maskElectricalAll
    }
}

class PortableNaNElement(_sixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor): SixNodeElement(_sixNode, side, descriptor) {
    val electricalLoad = NbtElectricalLoad("Portable NaN")

    val thermalLoad = NbtThermalLoad("Portable NaN")
    val descriptor: PortableNaNDescriptor
    init {
        this.descriptor = descriptor as PortableNaNDescriptor
        electricalLoadList.add(electricalLoad)
        thermalLoadList.add(thermalLoad)
        thermalLoad.setAsSlow()
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad {
        return electricalLoad
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad {
        thermalLoad.movePowerTo(Double.NaN)
        return thermalLoad
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        return descriptor.getNodeMask()
    }

    override fun initialize() {
        descriptor.applyTo(electricalLoad)
        descriptor.applyTo(thermalLoad)
    }

    override fun multiMeterString(): String {
        return Utils.plotUIP(electricalLoad.voltage, electricalLoad.current)
    }

    override fun thermoMeterString(): String {
        return Utils.plotCelsius("T", thermalLoad.temperatureCelsius)
    }

    override fun getWaila(): Map<String, String> {
        val info = HashMap<String, String>()

        info[tr("Current")] = Utils.plotAmpere("", electricalLoad.current)
        info[tr("Temperature")] = Utils.plotCelsius("", thermalLoad.temperature)
        if (Eln.wailaEasyMode) {
            info[tr("Voltage")] = Utils.plotVolt("", electricalLoad.voltage)
        }
        info[tr("Subsystem Matrix Size")] = Utils.renderSubSystemWaila(electricalLoad.subSystem)
        return info
    }
}

class PortableNaNRender(tileEntity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor): SixNodeElementRender(tileEntity, side, descriptor) {

    val descriptor: PortableNaNDescriptor

    init {
        this.descriptor = descriptor as PortableNaNDescriptor
    }

    override fun drawCableAuto(): Boolean {
        return false
    }

    override fun draw() {
        Minecraft.getInstance().profiler.push("ACable")

        UtilsClient.bindTexture(descriptor.render?.cableTexture)
        glListCall()

        GL11.glColor3f(1f, 1f, 1f)
        Minecraft.getInstance().profiler.pop()
    }

    override fun glListDraw() {
        CableRender.drawCable(descriptor.render!!, connectedSide, CableRender.connectionType(this, side))
        CableRender.drawNode(descriptor.render!!, connectedSide, CableRender.connectionType(this, side))
    }

    override fun glListEnable(): Boolean {
        return true
    }

    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? {
        return descriptor.render
    }
}

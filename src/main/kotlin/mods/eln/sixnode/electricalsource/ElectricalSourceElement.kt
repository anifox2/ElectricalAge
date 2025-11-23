package mods.eln.sixnode.electricalsource

import mods.eln.Eln
import mods.eln.i18n.I18N
import mods.eln.item.IConfigurable
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Utils
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNode
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElement
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.VoltageSource
import mods.eln.sim.nbt.NbtElectricalLoad
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.HashMap

class ElectricalSourceElement(
    sixNode: SixNode,
    side: Direction,
    descriptor: SixNodeDescriptor
) : SixNodeElement(sixNode, side, descriptor), IConfigurable {

    var electricalLoad = NbtElectricalLoad("electricalLoad")
    var voltageSource = VoltageSource("voltSrc", electricalLoad, null)

    companion object {
        const val setVoltageId: Byte = 1
        
        fun canBePlacedOnSide(side: Direction, type: Int): Boolean {
            return true
        }
    }

    init {
        electricalLoadList.add(electricalLoad)
        electricalComponentList.add(voltageSource)
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        voltageSource.voltage = nbt.getDouble("voltage")
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putDouble("voltage", voltageSource.voltage)
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? {
        return electricalLoad
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        return null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        return if ((sixNodeElementDescriptor as ElectricalSourceDescriptor).signalSource) {
            NodeBase.maskElectricalGate
        } else {
            NodeBase.maskElectricalPower
        }
    }

    override fun multiMeterString(): String {
        return Utils.plotUIP(electricalLoad.voltage, voltageSource.current)
    }

    override fun getWaila(): Map<String, String> {
        val info = HashMap<String, String>()
        info[I18N.tr("Voltage")] = Utils.plotVolt("", electricalLoad.voltage)
        info[I18N.tr("Current")] = Utils.plotAmpere("", electricalLoad.current)
        if (Eln.wailaEasyMode) {
            info[I18N.tr("Power")] = Utils.plotPower("", electricalLoad.voltage * electricalLoad.current)
        }
        return info
    }

    override fun thermoMeterString(): String {
        return ""
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        try {
            stream.writeFloat(voltageSource.voltage.toFloat())
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

    override fun networkUnserialize(stream: DataInputStream, player: ServerPlayer?) {
        super.networkUnserialize(stream, player)
        try {
            when (stream.readByte()) {
                setVoltageId -> {
                    voltageSource.voltage = stream.readFloat().toDouble()
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
        if (compound.contains("voltage")) {
            voltageSource.voltage = compound.getDouble("voltage")
            needPublish()
        }
    }

    override fun writeConfigTool(compound: CompoundTag, invoker: Player) {
        compound.putDouble("voltage", voltageSource.voltage)
    }
}

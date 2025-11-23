package mods.eln.sixnode.electricalcable

import mods.eln.Eln
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.item.BrushDescriptor
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Utils.addChatMessage
import mods.eln.misc.Utils.isPlayerUsingWrench
import mods.eln.misc.Utils.plotAmpere
import mods.eln.misc.Utils.plotCelsius
import mods.eln.misc.Utils.plotPower
import mods.eln.misc.Utils.plotUIP
import mods.eln.misc.Utils.plotVolt
import mods.eln.misc.Utils.renderSubSystemWaila
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNode
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElement
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sim.nbt.NbtThermalLoad
import mods.eln.sim.process.destruct.ThermalLoadWatchDog
import mods.eln.sim.process.destruct.VoltageStateWatchDog
import mods.eln.sim.process.destruct.WorldExplosion
import mods.eln.sim.process.heater.ElectricalLoadHeatThermalLoad
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import java.io.DataOutputStream
import java.io.IOException

open class ElectricalCableElement(_sixNode: SixNode?, side: Direction?, descriptor: SixNodeDescriptor) :
    SixNodeElement(_sixNode!!, side!!, descriptor) {

    var descriptor: ElectricalCableDescriptor
    var electricalLoad: NbtElectricalLoad? = NbtElectricalLoad("electricalLoad")
    var thermalLoad = NbtThermalLoad("thermalLoad")
    var heater = ElectricalLoadHeatThermalLoad(electricalLoad!!, thermalLoad)
    var thermalWatchdog = ThermalLoadWatchDog(thermalLoad)
    var voltageWatchdog = VoltageStateWatchDog(electricalLoad!!)
    var color: Int
    var colorCare: Int

    init {
        this.descriptor = descriptor as ElectricalCableDescriptor
        color = 0
        colorCare = 1
        electricalLoad!!.setCanBeSimplifiedByLine(true)
        electricalLoadList.add(electricalLoad!!)

        if (!this.descriptor.signalWire) {
            thermalLoadList.add(thermalLoad)
            thermalSlowProcessList.add(heater)
            thermalLoad.setAsSlow()
            slowProcessList.add(thermalWatchdog)
            thermalWatchdog
                .setTemperatureLimits(this.descriptor.thermalWarmLimit, this.descriptor.thermalCoolLimit)
                .setDestroys(WorldExplosion(this).cableExplosion())
        }

        slowProcessList.add(voltageWatchdog)
        voltageWatchdog
            .setNominalVoltage(this.descriptor.electricalNominalVoltage)
            .setDestroys(WorldExplosion(this).cableExplosion())
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        val b = nbt.getByte("color")
        color = b.toInt() and 0xF
        colorCare = b.toInt() shr 4 and 1
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putByte("color", (color + (colorCare shl 4)).toByte())
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? {
        return electricalLoad
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        return if (!descriptor.signalWire) thermalLoad else null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        return descriptor.getNodeMask() + (color shl NodeBase.maskColorShift) + (colorCare shl NodeBase.maskColorCareShift)
    }

    override fun multiMeterString(): String {
        return plotUIP(
            electricalLoad!!.voltage,
            electricalLoad!!.current
        ) + " " + plotPower(
            "Cable Power Loss",
            electricalLoad!!.current * electricalLoad!!.current * electricalLoad!!.serialResistance
        )
    }

    override fun getWaila(): Map<String, String> {
        val info: MutableMap<String, String> = HashMap()
        info[tr("Current")] = plotAmpere("", electricalLoad!!.current)
        if (!descriptor.signalWire) {
            info[tr("Temperature")] = plotCelsius("", thermalLoad.temperature)
        }
        if (Eln.wailaEasyMode) {
            info[tr("Voltage")] = plotVolt("", electricalLoad!!.voltage)
        }
        info[tr("Subsystem Matrix Size")] = renderSubSystemWaila(electricalLoad!!.subSystem)
        return info
    }

    override fun thermoMeterString(): String {
        return plotCelsius("T", thermalLoad.temperatureCelsius)
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        try {
            stream.writeByte(color shl 4)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun initialize() {
        descriptor.applyTo(electricalLoad!!)
        if (!descriptor.signalWire) descriptor.applyTo(thermalLoad)
    }

    override fun onBlockActivated(
        entityPlayer: Player,
        side: Direction,
        vx: Float,
        vy: Float,
        vz: Float
    ): Boolean {
        val currentItemStack = entityPlayer.mainHandItem
        if (isPlayerUsingWrench(entityPlayer)) {
            colorCare = colorCare xor 1
            addChatMessage(entityPlayer, "Wire color care $colorCare")
            sixNode!!.reconnect()
        } else if (!currentItemStack.isEmpty) {
            val gen = GenericItemUsingDamageDescriptor.getDescriptor(currentItemStack)
            if (gen is BrushDescriptor) {
                val brush = gen
                val brushColor = brush.getColor(currentItemStack)
                if (brushColor != color && brush.use(currentItemStack, entityPlayer)) {
                    color = brushColor
                    sixNode!!.reconnect()
                }
            }
        }
        return false
    }
}

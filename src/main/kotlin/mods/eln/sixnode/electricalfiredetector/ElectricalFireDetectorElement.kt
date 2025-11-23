package mods.eln.sixnode.electricalfiredetector

import mods.eln.i18n.I18N.tr
import mods.eln.item.electricalitem.BatteryItem
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Utils
import mods.eln.node.AutoAcceptInventoryProxy
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNode
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElement
import mods.eln.node.six.SixNodeElementInventory
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.nbt.NbtElectricalGateOutput
import mods.eln.sim.nbt.NbtElectricalGateOutputProcess
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import java.io.DataOutputStream
import java.io.IOException
import net.minecraft.network.chat.Component

// ...existing code...
class ElectricalFireDetectorElement(_sixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor) :
    SixNodeElement(_sixNode, side, descriptor) {

    val fireDescriptor get() = sixNodeElementDescriptor as ElectricalFireDetectorDescriptor
    var outputGate: NbtElectricalGateOutput? = null
    var outputGateProcess: NbtElectricalGateOutputProcess? = null
    var slowProcess: ElectricalFireDetectorSlowProcess

    var powered = false
    var firePresent = false

    var inventoryProxy: AutoAcceptInventoryProxy? = null
    override var inventory: Container? = null

    init {
        slowProcess = ElectricalFireDetectorSlowProcess(this)

        if (!fireDescriptor.batteryPowered) {
            powered = true
            outputGate = NbtElectricalGateOutput("outputGate")
            outputGateProcess = NbtElectricalGateOutputProcess("outputGateProcess", outputGate!!)
            electricalLoadList.add(outputGate!!)
            electricalComponentList.add(outputGateProcess!!)
        } else {
            powered = false
            inventory = SixNodeElementInventory(1, 64, this)
            inventoryProxy = AutoAcceptInventoryProxy(inventory!!)
                .acceptIfEmpty(0, BatteryItem::class.java)
        }

        slowProcessList.add(slowProcess)
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? {
        if (!fireDescriptor.batteryPowered && front == lrdu.left()) return outputGate
        return null
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        return null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        if (!fireDescriptor.batteryPowered && front == lrdu.left()) return NodeBase.maskElectricalOutputGate
        return 0;
    }

    override fun multiMeterString(): String {
        return if (fireDescriptor.batteryPowered) {
            tr("Fire detected: ") + firePresent
        } else {
            Utils.plotVolt("U:", outputGate!!.voltage) + Utils.plotAmpere("I:", outputGate!!.current)
        }
    }

    override fun getWaila(): Map<String, String> {
        val map = HashMap<String, String>()
        if (fireDescriptor.batteryPowered) {
            map[tr("Battery")] = Utils.plotPercent("Battery", slowProcess.getBatteryLevel())
            map[tr("Fire detected")] = firePresent.toString()
        } else {
            map[tr("Fire detected")] = (outputGate!!.voltage > 25.0).toString()
        }
        return map
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        try {
            stream.writeBoolean(powered)
            stream.writeBoolean(firePresent)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        fun canBePlacedOnSide(side: Direction, type: Int): Boolean {
            return true
        }
    }
}

package mods.eln.sixnode.lampsocket

import mods.eln.Eln
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.item.IConfigurable
import mods.eln.item.LampDescriptor
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
import mods.eln.sim.MonsterPopFreeProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.HashMap

// ...existing code...
class LampSocketElement(_sixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElement(_sixNode, side, descriptor), IConfigurable {

    var socketDescriptor: LampSocketDescriptor = descriptor as LampSocketDescriptor

    var monsterPopFreeProcess = MonsterPopFreeProcess(_sixNode.coordinate!!, Eln.instance.killMonstersAroundLampsRange)
// ...existing code...

    var positiveLoad = NbtElectricalLoad("positiveLoad")

    var lampProcess = LampSocketProcess(this)
    var lampResistor = Resistor(positiveLoad, null)

    var poweredByLampSupply = true
    var grounded = true

    var acceptingInventory: AutoAcceptInventoryProxy

    var lampDescriptor: LampDescriptor? = null
    var channel = lastSocketName

    var isConnectedToLampSupply = false

    var paintColor = 15

    init {
        inventory = SixNodeElementInventory(2, 64, this)
        acceptingInventory = AutoAcceptInventoryProxy(inventory!!)
            .acceptIfEmpty(0, LampDescriptor::class.java)
            .acceptIfEmpty(1, ElectricalCableDescriptor::class.java)

        lampProcess.alphaZ = this.socketDescriptor.alphaZBoot
        slowProcessList.add(lampProcess)
        slowProcessList.add(monsterPopFreeProcess)
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        val value = nbt.getByte("front").toInt()
        front = LRDU.fromInt((value shr 0) and 0x3)
        grounded = (value and 4) != 0

        setPoweredByLampSupply(nbt.getBoolean("poweredByLampSupply"))
        channel = nbt.getString("channel")

        val b = nbt.getByte("color").toInt()
        if (socketDescriptor.paintable)
            paintColor = b and 0xF
        else {
            paintColor = 0x0F
        }
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putByte("front", ((front!!.toInt() shl 0) + (if (grounded) 4 else 0)).toByte())
        nbt.putBoolean("poweredByLampSupply", poweredByLampSupply)
        nbt.putString("channel", channel)
        nbt.putByte("color", paintColor.toByte())
    }

    override fun networkUnserialize(stream: DataInputStream) {
        try {
            when (stream.readByte().toInt()) {
                setGroundedId -> {
                    grounded = stream.readByte().toInt() != 0
                    computeElectricalLoad()
                    reconnect()
                }
                setAlphaZId -> {
                    lampProcess.alphaZ = stream.readFloat().toDouble()
                    needPublish()
                }
                tooglePowerSupplyType -> {
                    setPoweredByLampSupply(!poweredByLampSupply)
                    reconnect()
                }
                setChannel -> {
                    channel = stream.readUTF()
                    lastSocketName = channel
                    needPublish()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setPoweredByLampSupply(b: Boolean) {
        poweredByLampSupply = b
    }

    override fun disconnectJob() {
        super.disconnectJob()

        electricalLoadList.remove(positiveLoad)
        electricalComponentList.remove(lampResistor)
        positiveLoad.state = 0.0
    }

    override fun connectJob() {
        if (!poweredByLampSupply) {
            electricalLoadList.add(positiveLoad)
            electricalComponentList.add(lampResistor)
        }
        super.connectJob()
    }

    override fun inventoryChanged() {
        computeElectricalLoad()
        reconnect()
    }

    override fun hasGui(): Boolean {
        return true;
    }

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return LampSocketContainer(player, inventory!!, socketDescriptor)
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? {
        if (inventory!!.getItem(LampSocketContainer.cableSlotId).isEmpty) return null
        if (poweredByLampSupply) return null

        if (grounded) return positiveLoad
        return null
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        return null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        if (inventory!!.getItem(LampSocketContainer.cableSlotId).isEmpty) return 0
        if (poweredByLampSupply) return 0
        if (grounded) return NodeBase.maskElectricalPower

        if (front == lrdu) return NodeBase.maskElectricalPower
        if (front == lrdu.inverse()) return NodeBase.maskElectricalPower

        return 0
    }

    override fun multiMeterString(): String {
        return Utils.plotVolt("U:", positiveLoad.voltage) + Utils.plotAmpere("I:", lampResistor.current)
    }

    override fun getWaila(): Map<String, String> {
        val info = HashMap<String, String>()
        info[tr("Power consumption")] = Utils.plotPower("", lampResistor.current * lampResistor.voltage)
        val lampStack = inventory!!.getItem(0)
        if (lampDescriptor != null && !lampStack.isEmpty) {
            info[tr("Bulb")] = lampStack.hoverName.string
        } else {
            info[tr("Bulb")] = tr("None")
        }
        if (Eln.wailaEasyMode) {
            if (poweredByLampSupply) {
                info[tr("Channel")] = channel
            }
            info[tr("Voltage")] = Utils.plotVolt("", positiveLoad.voltage)
            if (!lampStack.isEmpty && lampDescriptor != null) {
                info[tr("Life Left: ")] = Utils.plotValue(lampDescriptor!!.getLifeInTag(lampStack)) + " Hours"
            }

        }
        return info
    }

    override fun thermoMeterString(): String? {
        return null
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        try {
            stream.writeByte((if (grounded) (1 shl 6) else 0))
            Utils.serialiseItemStack(stream, inventory!!.getItem(LampSocketContainer.lampSlotId))
            stream.writeFloat(lampProcess.alphaZ.toFloat())
            Utils.serialiseItemStack(stream, inventory!!.getItem(LampSocketContainer.cableSlotId))
            stream.writeBoolean(poweredByLampSupply)
            stream.writeUTF(channel)
            stream.writeBoolean(isConnectedToLampSupply)
            stream.writeByte(lampProcess.light)
            stream.writeByte(paintColor)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun initialize() {
        computeElectricalLoad()
    }

    fun computeElectricalLoad() {
        val lamp = inventory!!.getItem(LampSocketContainer.lampSlotId)
        val cable = inventory!!.getItem(LampSocketContainer.cableSlotId)

        val cableDescriptor = GenericItemUsingDamageDescriptor.getDescriptor(cable) as? ElectricalCableDescriptor

        if (cableDescriptor == null) {
            positiveLoad.highImpedance()
        } else {
            cableDescriptor.applyTo(positiveLoad)
        }

        lampDescriptor = GenericItemUsingDamageDescriptor.getDescriptor(lamp) as? LampDescriptor

        if (lampDescriptor == null) {
            lampResistor.resistance = Double.POSITIVE_INFINITY
        } else {
            lampDescriptor!!.applyTo(lampResistor)
        }
    }

    override fun onBlockActivated(entityPlayer: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        if (Utils.isPlayerUsingWrench(entityPlayer)) {
            front = front!!.nextClockwise()
            if (socketDescriptor.rotateOnlyBy180Deg)
                front = front!!.nextClockwise()
            reconnect()
            return true
        }
        return super.onBlockActivated(entityPlayer, side, vx, vy, vz)
    }
    
    fun setIsConnectedToLampSupply(connected: Boolean) {
        isConnectedToLampSupply = connected
    }

    override fun readConfigTool(compound: CompoundTag, invoker: Player) {
        // TODO: Implement if needed
    }

    override fun writeConfigTool(compound: CompoundTag, invoker: Player) {
        // TODO: Implement if needed
    }

    companion object {
        var lastSocketName = "Default channel"
        const val setGroundedId = 1
        const val setAlphaZId = 2
        const val tooglePowerSupplyType = 3
        const val setChannel = 4
    }
}

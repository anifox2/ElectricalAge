package mods.eln.sixnode.modbusrtu

import mods.eln.Eln
import mods.eln.i18n.I18N
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Utils
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNode
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElement
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.nbt.NbtElectricalGateInputOutput
import mods.eln.sim.nbt.NbtElectricalGateOutputProcess
import mods.eln.sixnode.wirelesssignal.IWirelessSignalSpot
import mods.eln.sixnode.wirelesssignal.IWirelessSignalTx
import mods.eln.sixnode.wirelesssignal.WirelessUtils
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import net.minecraft.nbt.CompoundTag
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

class ModbusRtuElement(sixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElement(sixNode, side, descriptor), IModbusSlave {

    var ioGate = Array(4) { i -> NbtElectricalGateInputOutput("ioGate$i") }
    var ioGateProcess = Array(4) { i -> NbtElectricalGateOutputProcess("ioGateProcess$i", ioGate[i]) }

    var wirelessTxStatusList = HashMap<Int, ServerWirelessTxStatus>()
    var wirelessRxStatusList = HashMap<Int, ServerWirelessRxStatus>()

    var descriptor: ModbusRtuDescriptor = descriptor as ModbusRtuDescriptor

    companion object {
        const val ioStartOffset = 16
        const val ioRange = 8
        const val setStation: Byte = 1
        const val setName: Byte = 2
        const val serverTxAdd: Byte = 3
        const val serverRxAdd: Byte = 4
        const val serverTxConfig: Byte = 5
        const val serverRxConfig: Byte = 6
        const val serverTxDelete: Byte = 7
        const val serverRxDelete: Byte = 8
        const val serverAllSyncronise: Byte = 9

        const val clientAllSyncronise: Byte = 1
        const val clientTx1Syncronise: Byte = 2
        const val clientRx1Syncronise: Byte = 3
        const val clientTxDelete: Byte = 4
        const val clientRxDelete: Byte = 5
        const val clientRx1Connected: Byte = 6

        const val ClientModbusActivityEvent: Byte = 7
        const val ClientModbusErrorEvent: Byte = 8
    }

    var station = -1
        set(value) {
            removeFromServer()
            field = value
            addToServer()
            needPublish()
        }
    override val slaveId: Int get() = station
    var name = ""
        set(value) {
            field = value
            needPublish()
        }

    var txSet = HashMap<String, HashSet<IWirelessSignalTx>>()
    var txStrength = HashMap<IWirelessSignalTx, Double>()

    var addedOnServer = false

    var mapping = ArrayList<IModbusSlot>()
    var nullSlot = ModbusNullSlot()

    init {
        for (idx in 0 until 4) {
            electricalLoadList.add(ioGate[idx])
            electricalComponentList.add(ioGateProcess[idx])

            ioGateProcess[idx].setHighImpedance(true)

            mapping.add(ModbusAnalogIoSlot(ioStartOffset + idx * ioRange, ioRange, ioGate[idx], ioGateProcess[idx]))
        }

        slowProcessList.add(ModbusRtuSlowProcess())
    }

    inner class ModbusRtuSlowProcess : IProcess {
        var sleepTimer = 0.0

        override fun process(time: Double) {
            sleepTimer -= time
            if (sleepTimer < 0) {
                sleepTimer += Utils.rand(1.2, 2.0)

                val spot = WirelessUtils.buildSpot(coordinate, null, 0)
                WirelessUtils.getTx(spot, txSet, txStrength)
            }

            for (rx in wirelessRxStatusList.values) {
                if (rx.isConnected() != rx.connected) {
                    rx.connected = !rx.connected
                    sendRx1Connected(rx)
                }
            }
        }
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? {
        return ioGate[lrdu.toInt()]
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        return null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        return NodeBase.maskElectricalGate
    }

    override fun multiMeterString(): String {
        return "" // Utils.plotUIP(powerLoad.Uc, powerLoad.getCurrent());
    }

    override fun getWaila(): Map<String, String> {
        val info = HashMap<String, String>()
        if (Eln.modbusEnable) {
            info[I18N.tr("Modbus TCP")] = "${Eln.modbusServer.host}:${Eln.modbusServer.port}"
            info[I18N.tr("Modbus Unit ID")] = station.toString()
        } else {
            info["X_X"] = I18N.tr("Modbus is disabled, enable it in Eln.cfg")
        }
        return info
    }

    override fun thermoMeterString(): String {
        return ""
    }

    override fun initialize() {
        addToServer()
        // connect();
    }

    override fun destroy(entityPlayer: ServerPlayer?) {
        super.destroy(entityPlayer)
        unregister()
    }

    override fun unload() {
        super.unload()
        unregister()
    }

    fun unregister() {
        removeFromServer()

        // Remove all TX signals.
        val keys = ArrayList(wirelessTxStatusList.keys)
        for (key in keys) {
            val status = wirelessTxStatusList[key]
            status?.delete()
            wirelessTxStatusList.remove(key)
        }
    }

    override fun onBlockActivated(entityPlayer: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        if (Utils.isPlayerUsingWrench(entityPlayer)) {
            if (side.isY) {
                front = front!!.nextClockwise
                sixNode!!.reconnect()
                sixNode!!.needPublish = true
            }
            return true
        } else {
            return false
        }
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)

        try {
            stream.writeInt(station)
            stream.writeUTF(name)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendTx1Syncronise(tx: WirelessTxStatus) {
        val bos = ByteArrayOutputStream(64)
        val packet = DataOutputStream(bos)

        preparePacketForClient(packet)

        try {
            packet.writeByte(clientTx1Syncronise.toInt())
            tx.writeTo(packet)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        sendPacketToAllClient(bos)
    }

    fun sendRx1Syncronise(rx: WirelessRxStatus) {
        val bos = ByteArrayOutputStream(64)
        val packet = DataOutputStream(bos)

        preparePacketForClient(packet)

        try {
            packet.writeByte(clientRx1Syncronise.toInt())
            rx.writeTo(packet)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        sendPacketToAllClient(bos)
    }

    fun sendRx1Connected(rx: WirelessRxStatus) {
        val bos = ByteArrayOutputStream(64)
        val packet = DataOutputStream(bos)

        preparePacketForClient(packet)

        try {
            packet.writeByte(clientRx1Connected.toInt())
            packet.writeInt(rx.uuid)
            packet.writeBoolean(rx.connected)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        sendPacketToAllClient(bos)
    }

    fun onActivity() {
        val bos = ByteArrayOutputStream(64)
        val packet = DataOutputStream(bos)

        preparePacketForClient(packet)

        try {
            packet.writeByte(ClientModbusActivityEvent.toInt())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        sendPacketToAllClient(bos)
    }

    fun onError() {
        val bos = ByteArrayOutputStream(64)
        val packet = DataOutputStream(bos)

        preparePacketForClient(packet)

        try {
            packet.writeByte(ClientModbusErrorEvent.toInt())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        sendPacketToAllClient(bos)
    }

    override fun networkUnserialize(stream: DataInputStream, player: ServerPlayer?) {
        super.networkUnserialize(stream, player)
        try {
            when (stream.readByte()) {
                setStation -> this.station = stream.readInt()
                setName -> this.name = stream.readUTF()
                serverTxAdd -> {
                    val name = stream.readUTF()
                    var uuid = 0
                    for (tx in wirelessTxStatusList.values) {
                        uuid = Math.max(uuid, tx.uuid)
                    }
                    uuid++
                    val tx = ServerWirelessTxStatus(name, -1, 0.0, sixNode!!.coordinate!!, uuid, this)
                    wirelessTxStatusList[uuid] = tx
                    sendTx1Syncronise(tx)
                }
                serverRxAdd -> {
                    val name = stream.readUTF()
                    var uuid = 0
                    for (rx in wirelessRxStatusList.values) {
                        uuid = Math.max(uuid, rx.uuid)
                    }
                    uuid++
                    val rx = ServerWirelessRxStatus(name, -1, false, uuid, this)
                    wirelessRxStatusList[uuid] = rx
                    sendRx1Syncronise(rx)
                }
                serverTxConfig -> {
                    val uuid = stream.readInt()
                    val name = stream.readUTF()
                    val id = stream.readInt()

                    val tx = wirelessTxStatusList[uuid]
                    if (tx != null) {
                        tx.name = name
                        tx.id = id
                        sendTx1Syncronise(tx)
                    }
                }
                serverRxConfig -> {
                    val uuid = stream.readInt()
                    val name = stream.readUTF()
                    val id = stream.readInt()

                    val rx = wirelessRxStatusList[uuid]
                    if (rx != null) {
                        rx.name = name
                        rx.id = id
                        sendRx1Syncronise(rx)
                    }
                }
                serverTxDelete -> {
                    val uuid = stream.readInt()
                    val tx = wirelessTxStatusList[uuid]
                    if (tx != null) {
                        tx.delete()
                        wirelessTxStatusList.remove(tx.uuid)

                        val bos = ByteArrayOutputStream(64)
                        val packet = DataOutputStream(bos)

                        preparePacketForClient(packet)

                        packet.writeByte(clientTxDelete.toInt())
                        packet.writeInt(uuid)

                        sendPacketToAllClient(bos)
                    }
                }
                serverRxDelete -> {
                    val uuid = stream.readInt()
                    val rx = wirelessRxStatusList[uuid]
                    if (rx != null) {
                        rx.delete()
                        wirelessRxStatusList.remove(uuid)

                        val bos = ByteArrayOutputStream(64)
                        val packet = DataOutputStream(bos)

                        preparePacketForClient(packet)

                        packet.writeByte(clientRxDelete.toInt())
                        packet.writeInt(uuid)

                        sendPacketToAllClient(bos)
                    }
                }
                serverAllSyncronise -> {
                    val bos = ByteArrayOutputStream(64)
                    val packet = DataOutputStream(bos)

                    preparePacketForClient(packet)

                    packet.writeByte(clientAllSyncronise.toInt())

                    packet.writeInt(wirelessTxStatusList.size)
                    for (e in wirelessTxStatusList.values) {
                        e.writeTo(packet)
                    }

                    packet.writeInt(wirelessRxStatusList.size)
                    for (e in wirelessRxStatusList.values) {
                        e.writeTo(packet)
                    }

                    sendPacketToClient(bos, player!!)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putInt("station", station)
        nbt.putString("name", name)

        nbt.putInt("txCnt", wirelessTxStatusList.size)
        var idx = 0
        for (tx in wirelessTxStatusList.values) {
            tx.writeToNBT(nbt, "tx$idx")
            idx++
        }

        nbt.putInt("rxCnt", wirelessRxStatusList.size)
        idx = 0
        for (rx in wirelessRxStatusList.values) {
            rx.writeToNBT(nbt, "rx$idx")
            idx++
        }
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        station = nbt.getInt("station")
        name = nbt.getString("name")

        var cnt = nbt.getInt("txCnt")
        for (idx in 0 until cnt) {
            val tx = ServerWirelessTxStatus(nbt, "tx$idx", this)
            wirelessTxStatusList[tx.uuid] = tx
        }
        cnt = nbt.getInt("rxCnt")
        for (idx in 0 until cnt) {
            val rx = ServerWirelessRxStatus(nbt, "rx$idx", this)
            wirelessRxStatusList[rx.uuid] = rx
        }
    }

    override fun hasGui(): Boolean {
        return true
    }

    fun addToServer() {
        if (station != -1) {
            addedOnServer = Eln.modbusServer.add(this)
        }
    }

    fun removeFromServer() {
        if (addedOnServer)
            Eln.modbusServer.remove(this)
        addedOnServer = false
    }

    fun getModbusSlot(id: Int): IModbusSlot {
        for (slot in mapping) {
            if (id >= slot.offset && id < slot.offset + slot.size) {
                onActivity()
                return slot
            }
        }
        onError()
        return nullSlot
    }

    override fun getCoil(id: Int): Boolean {
        val slot = getModbusSlot(id)
        return slot.getCoil(id - slot.offset)
    }

    override fun getInput(id: Int): Boolean {
        val slot = getModbusSlot(id)
        return slot.getInput(id - slot.offset)
    }

    override fun getInputRegister(id: Int): Short {
        val slot = getModbusSlot(id)
        return slot.getInputRegister(id - slot.offset)
    }

    override fun getHoldingRegister(id: Int): Short {
        val slot = getModbusSlot(id)
        return slot.getHoldingRegister(id - slot.offset)
    }



    override fun setCoil(id: Int, value: Boolean) {
        val slot = getModbusSlot(id)
        slot.setCoil(id - slot.offset, value)
    }

    override fun setHoldingRegister(id: Int, value: Short) {
        val slot = getModbusSlot(id)
        slot.setHoldingRegister(id - slot.offset, value)
    }
}

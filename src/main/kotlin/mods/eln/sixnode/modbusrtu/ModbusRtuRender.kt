package mods.eln.sixnode.modbusrtu

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.PhysicalInterpolator
import mods.eln.misc.Utils
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import java.io.DataInputStream
import java.io.IOException
import java.util.HashMap

class ModbusRtuRender(
    blockEntity: SixNodeEntity,
    side: Direction,
    descriptor: SixNodeDescriptor
) : SixNodeElementRender(blockEntity, side, descriptor) {

    var coord: Coordinate = Coordinate(blockEntity)
    var interpolator: PhysicalInterpolator = PhysicalInterpolator(0.4f, 8.0f, 0.9f, 0.2f)
    var modbusActivityTimeout: Float = 0f
    var modbusErrorTimeout: Float = 0f

    var descriptor: ModbusRtuDescriptor = descriptor as ModbusRtuDescriptor

    var wirelessTxStatusList = HashMap<Int, WirelessTxStatus>()
    var wirelessRxStatusList = HashMap<Int, WirelessRxStatus>()

    var station: Int = -1
    var name: String = ""
    var boot: Boolean = true

    var rxTxChange: Boolean = false

    override fun draw() {
        super.draw()

        if (side.isY) {
            front!!.inverse().glRotateOnX()
        } else {
            LRDU.Down.glRotateOnX()
        }

        descriptor.draw(interpolator.get(), true, modbusActivityTimeout > 0, modbusErrorTimeout > 0)
    }

    override fun refresh(deltaT: Float) {
        if (!Utils.isPlayerAround(blockEntity.level!!, coord.getAABB(0.0)))
            interpolator.target = 0f
        else
            interpolator.target = 1f

        interpolator.step(deltaT)

        if (modbusActivityTimeout > 0)
            modbusActivityTimeout -= deltaT

        if (modbusErrorTimeout > 0)
            modbusErrorTimeout -= deltaT
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)

        try {
            station = stream.readInt()
            name = stream.readUTF()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (boot)
            clientSend(ModbusRtuElement.serverAllSyncronise.toInt())
        boot = false
    }

    @Throws(IOException::class)
    override fun serverPacketUnserialize(stream: DataInputStream?) {
        super.serverPacketUnserialize(stream)

        if (stream == null) return

        when (stream.readByte()) {
            ModbusRtuElement.clientAllSyncronise -> {
                wirelessTxStatusList.clear()
                var idx = stream.readInt()
                while (idx > 0) {
                    val tx = WirelessTxStatus()
                    tx.readFrom(stream)
                    wirelessTxStatusList[tx.uuid] = tx
                    idx--
                }
                wirelessRxStatusList.clear()
                idx = stream.readInt()
                while (idx > 0) {
                    val rx = WirelessRxStatus()
                    rx.readFrom(stream)
                    wirelessRxStatusList[rx.uuid] = rx
                    idx--
                }
                rxTxChange = true
            }
            ModbusRtuElement.clientTx1Syncronise -> {
                val tx = WirelessTxStatus()
                tx.readFrom(stream)
                wirelessTxStatusList[tx.uuid] = tx
                rxTxChange = true
            }
            ModbusRtuElement.clientRx1Syncronise -> {
                val rx = WirelessRxStatus()
                rx.readFrom(stream)
                wirelessRxStatusList[rx.uuid] = rx
                rxTxChange = true
            }
            ModbusRtuElement.clientTxDelete -> {
                val uuid = stream.readInt()
                wirelessTxStatusList.remove(uuid)
                rxTxChange = true
            }
            ModbusRtuElement.clientRxDelete -> {
                val uuid = stream.readInt()
                wirelessRxStatusList.remove(uuid)
                rxTxChange = true
            }
            ModbusRtuElement.clientRx1Connected -> {
                val uuid = stream.readInt()
                val connected = stream.readBoolean()
                val rx = wirelessRxStatusList[uuid]
                if (rx != null) rx.connected = connected
                rxTxChange = true
            }
            ModbusRtuElement.ClientModbusActivityEvent -> {
                modbusActivityTimeout = 0.2f
            }
            ModbusRtuElement.ClientModbusErrorEvent -> {
                modbusErrorTimeout = 0.4f
            }
        }
    }

    fun clientSend(id: Byte, value: Int) {
        val bos = ByteArrayOutputStream()
        val stream = DataOutputStream(bos)
        preparePacketForServer(stream)
        try {
            stream.writeByte(id.toInt())
            stream.writeInt(value)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sendPacketToServer(bos)
    }

    fun clientSend(id: Byte, value: String) {
        val bos = ByteArrayOutputStream()
        val stream = DataOutputStream(bos)
        preparePacketForServer(stream)
        try {
            stream.writeByte(id.toInt())
            stream.writeUTF(value)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sendPacketToServer(bos)
    }

    fun clientSend(id: Byte, uuid: Int, name: String, txId: Int) {
        val bos = ByteArrayOutputStream()
        val stream = DataOutputStream(bos)
        preparePacketForServer(stream)
        try {
            stream.writeByte(id.toInt())
            stream.writeInt(uuid)
            stream.writeUTF(name)
            stream.writeInt(txId)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sendPacketToServer(bos)
    }
}

package mods.eln.sixnode.modbusrtu

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.misc.Utils
import mods.eln.sixnode.wirelesssignal.IWirelessSignalTx
import mods.eln.sixnode.wirelesssignal.tx.WirelessSignalTxElement
import net.minecraft.nbt.CompoundTag

class ServerWirelessTxStatus : WirelessTxStatus, IWirelessSignalTx, IModbusSlot {

    var rtu: ModbusRtuElement? = null
    var txCoordinate: Coordinate? = null

    var getHoldingRegister_1: Short = 0
    var setHoldingRegister_0: Short = 0

    constructor(name: String, id: Int, value: Double, coordinate: Coordinate, uuid: Int, rtu: ModbusRtuElement) : super(name, id, value, uuid) {
        this.txCoordinate = coordinate
        WirelessSignalTxElement.channelRegister(this)
        this.rtu = rtu
        rtu.mapping.add(this)
    }

    constructor(nbt: CompoundTag, str: String, rtu: ModbusRtuElement) : super() {
        readFromNBT(nbt, str)
        this.txCoordinate = rtu.sixNode?.coordinate
        WirelessSignalTxElement.channelRegister(this)
        this.rtu = rtu
        rtu.mapping.add(this)
    }

    override fun getCoordinate(): Coordinate? {
        return txCoordinate
    }

    override var name: String
        get() = super.name
        set(value) {
            WirelessSignalTxElement.channelRemove(this)
            super.name = value
            WirelessSignalTxElement.channelRegister(this)
        }

    fun delete() {
        rtu?.mapping?.remove(this)
        WirelessSignalTxElement.channelRemove(this)
    }



    override fun getRange(): Int {
        return Eln.wirelessTxRange
    }

    override fun getChannel(): String {
        return name
    }



    override val offset: Int
        get() = id

    override val size: Int
        get() = 4

    override fun getCoil(id: Int): Boolean {
        return when (id) {
            1 -> value >= 0.5
            else -> false
        }
    }

    override fun getHoldingRegister(id: Int): Short {
        return when (id) {
            1 -> {
                val v = value
                getHoldingRegister_1 = Utils.modbusToShort(v.toFloat(), 1)
                Utils.modbusToShort(v.toFloat(), 0)
            }
            2 -> getHoldingRegister_1
            3 -> (65535.0 * value).toInt().toShort()
            else -> 0
        }
    }

    override fun getInput(id: Int): Boolean = false
    override fun getInputRegister(id: Int): Short = 0

    override fun setCoil(id: Int, value: Boolean) {}
    override fun setHoldingRegister(id: Int, value: Short) {}
    override fun setInput(id: Int, value: Boolean) {}
    override fun setInputRegister(id: Int, value: Short) {}

    override fun writeCoil(id: Int, value: Boolean) {}
    override fun writeHoldingRegister(id: Int, value: Short) {}
}

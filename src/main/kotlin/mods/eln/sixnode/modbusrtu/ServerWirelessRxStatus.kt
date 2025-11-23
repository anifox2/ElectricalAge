package mods.eln.sixnode.modbusrtu

import mods.eln.misc.Utils
import mods.eln.sixnode.wirelesssignal.aggregator.BiggerAggregator
import net.minecraft.nbt.CompoundTag

class ServerWirelessRxStatus : WirelessRxStatus, IModbusSlot {

    var rtu: ModbusRtuElement? = null
    var aggregator = BiggerAggregator()

    var getInputRegister_1: Short = 0

    constructor(name: String, id: Int, connected: Boolean, uuid: Int, rtu: ModbusRtuElement) : super(name, id, connected, uuid) {
        this.rtu = rtu
        rtu.mapping.add(this)
    }

    constructor(nbt: CompoundTag, str: String, rtu: ModbusRtuElement) : super() {
        readFromNBT(nbt, str)
        this.rtu = rtu
        rtu.mapping.add(this)
    }

    fun delete() {
        rtu?.mapping?.remove(this)
    }

    fun isConnected(): Boolean {
        return rtu?.txSet?.get(name) != null
    }

    fun readWireless(): Double {
        if (!isConnected()) return 0.0
        return aggregator.aggregate(rtu!!.txSet[name])
    }

    override val offset: Int
        get() = id

    override val size: Int
        get() = 4

    override fun getCoil(id: Int): Boolean {
        return getInput(id)
    }

    override fun getHoldingRegister(id: Int): Short {
        return getInputRegister(id)
    }

    override fun getInput(id: Int): Boolean {
        return when (id) {
            0 -> isConnected()
            1 -> readWireless() >= 0.5
            else -> false
        }
    }

    override fun getInputRegister(id: Int): Short {
        return when (id) {
            0 -> if (isConnected()) 1 else 0
            1 -> {
                val v = readWireless()
                getInputRegister_1 = Utils.modbusToShort(v.toFloat(), 1)
                Utils.modbusToShort(v.toFloat(), 0)
            }
            2 -> getInputRegister_1
            3 -> (65535.0 * readWireless()).toInt().toShort()
            else -> 0
        }
    }

    override fun setCoil(id: Int, value: Boolean) {}
    override fun setHoldingRegister(id: Int, value: Short) {}
    override fun setInput(id: Int, value: Boolean) {}
    override fun setInputRegister(id: Int, value: Short) {}

    override fun writeCoil(id: Int, value: Boolean) {}
    override fun writeHoldingRegister(id: Int, value: Short) {}
}

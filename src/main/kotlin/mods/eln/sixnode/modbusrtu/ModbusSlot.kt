package mods.eln.sixnode.modbusrtu

abstract class ModbusSlot(override val offset: Int, val range: Int) : IModbusSlot {
    override val size: Int
        get() = range

    override fun writeCoil(id: Int, value: Boolean) {
        setCoil(id, value)
    }

    override fun writeHoldingRegister(id: Int, value: Short) {
        setHoldingRegister(id, value)
    }
    
    override fun setInput(id: Int, value: Boolean) {}
    override fun setInputRegister(id: Int, value: Short) {}
}

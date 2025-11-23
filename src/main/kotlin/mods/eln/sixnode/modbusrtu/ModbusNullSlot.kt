package mods.eln.sixnode.modbusrtu

class ModbusNullSlot : IModbusSlot {
    override val offset: Int = 0
    override val size: Int = 0

    override fun getCoil(id: Int): Boolean = false
    override fun getHoldingRegister(id: Int): Short = 0
    override fun getInput(id: Int): Boolean = false
    override fun getInputRegister(id: Int): Short = 0

    override fun setCoil(id: Int, value: Boolean) {}
    override fun setHoldingRegister(id: Int, value: Short) {}
    override fun setInput(id: Int, value: Boolean) {}
    override fun setInputRegister(id: Int, value: Short) {}

    override fun writeCoil(id: Int, value: Boolean) {}
    override fun writeHoldingRegister(id: Int, value: Short) {}
}

package mods.eln.sixnode.modbusrtu

interface IModbusSlot {
    val offset: Int
    val size: Int

    fun getCoil(id: Int): Boolean
    fun getHoldingRegister(id: Int): Short
    fun getInput(id: Int): Boolean
    fun getInputRegister(id: Int): Short

    fun setCoil(id: Int, value: Boolean)
    fun setHoldingRegister(id: Int, value: Short)
    fun setInput(id: Int, value: Boolean)
    fun setInputRegister(id: Int, value: Short)

    fun writeCoil(id: Int, value: Boolean)
    fun writeHoldingRegister(id: Int, value: Short)
}

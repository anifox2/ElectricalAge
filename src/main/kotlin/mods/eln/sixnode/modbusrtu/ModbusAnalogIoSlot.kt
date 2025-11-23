package mods.eln.sixnode.modbusrtu

import mods.eln.misc.Utils
import mods.eln.sim.nbt.NbtElectricalGateInputOutput
import mods.eln.sim.nbt.NbtElectricalGateOutputProcess

class ModbusAnalogIoSlot(
    offset: Int,
    range: Int,
    var gate: NbtElectricalGateInputOutput,
    var gateProcess: NbtElectricalGateOutputProcess
) : ModbusSlot(offset, range) {

    var getHoldingRegister_2: Short = 0
    var getInputRegister_2: Short = 0
    var setHoldingRegister_1: Short = 0

    override fun getCoil(id: Int): Boolean {
        return when (id) {
            0 -> gateProcess.isHighImpedance()
            1 -> gateProcess.outputOnOff
            else -> false
        }
    }

    override fun getHoldingRegister(id: Int): Short {
        return when (id) {
            1 -> {
                val f = gateProcess.outputNormalized
                getHoldingRegister_2 = Utils.modbusToShort(f.toFloat(), 1)
                Utils.modbusToShort(f.toFloat(), 0)
            }
            2 -> getHoldingRegister_2
            3 -> (65535.0 * gateProcess.outputNormalized).toInt().toShort()
            else -> 0
        }
    }

    override fun getInput(id: Int): Boolean {
        return when (id) {
            1 -> gate.isInputHigh
            else -> false
        }
    }

    override fun getInputRegister(id: Int): Short {
        return when (id) {
            1 -> {
                val f = gate.inputNormalized
                getInputRegister_2 = Utils.modbusToShort(f.toFloat(), 1)
                Utils.modbusToShort(f.toFloat(), 0)
            }
            2 -> getInputRegister_2
            3 -> (65535.0 * gate.inputNormalized).toInt().toShort()
            else -> 0
        }
    }

    override fun setCoil(id: Int, value: Boolean) {
        when (id) {
            0 -> gateProcess.setHighImpedance(value)
            1 -> gateProcess.state(value)
        }
    }

    override fun setHoldingRegister(id: Int, value: Short) {
        when (id) {
            1 -> setHoldingRegister_1 = value
            2 -> gateProcess.outputNormalized = Utils.modbusToFloat(setHoldingRegister_1, value).toDouble()
            3 -> gateProcess.outputNormalized = (value.toInt() and 0xFFFF) / 65535.0
        }
    }
}

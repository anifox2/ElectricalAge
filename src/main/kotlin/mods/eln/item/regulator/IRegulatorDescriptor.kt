package mods.eln.item.regulator

import mods.eln.item.GenericItemUsingDamageDescriptorUpgrade
import mods.eln.sim.RegulatorProcess

abstract class IRegulatorDescriptor(name: String) : GenericItemUsingDamageDescriptorUpgrade(name) {

    enum class RegulatorType {
        Manual, None, OnOff, Analog
    }

    abstract fun getType(): RegulatorType

    abstract fun applyTo(regulator: RegulatorProcess, workingPoint: Double, P: Double, I: Double, D: Double)
}

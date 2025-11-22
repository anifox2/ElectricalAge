package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.sim.process.RegulatorProcess

abstract class IRegulatorDescriptor(name: String) : GenericItemUsingDamageDescriptor(name) {
    enum class RegulatorType {
        Analog, OnOff
    }

    abstract fun applyTo(process: RegulatorProcess, maxPower: Double, maxTemperature: Double, gain: Double, integration: Double)
}

package mods.eln.item.regulator

import mods.eln.sim.RegulatorProcess
import mods.eln.item.regulator.IRegulatorDescriptor.RegulatorType

class RegulatorAnalogDescriptor(name: String, iconName: String) : IRegulatorDescriptor(name) {

    init {
        setDefaultIcon(iconName)
    }

    override fun getType(): RegulatorType {
        return RegulatorType.Analog
    }

    override fun applyTo(regulator: RegulatorProcess, workingPoint: Double, P: Double, I: Double, D: Double) {
        regulator.setAnalog(P, I, D, workingPoint)
    }
}

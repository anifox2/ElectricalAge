package mods.eln.item.regulator

import mods.eln.sim.RegulatorProcess
import mods.eln.item.regulator.IRegulatorDescriptor.RegulatorType

class RegulatorOnOffDescriptor(name: String, iconName: String, private val hysteresis: Double) : IRegulatorDescriptor(name) {

    init {
        setDefaultIcon(iconName)
    }

    override fun getType(): RegulatorType {
        return RegulatorType.OnOff
    }

    override fun applyTo(regulator: RegulatorProcess, workingPoint: Double, P: Double, I: Double, D: Double) {
        regulator.setOnOff(hysteresis, workingPoint)
    }
}

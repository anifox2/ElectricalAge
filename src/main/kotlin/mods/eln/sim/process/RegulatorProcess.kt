package mods.eln.sim.process

import mods.eln.sim.IProcess

abstract class RegulatorProcess(val name: String) : IProcess {
    var target: Double = 0.0
    open fun getHit(): Double = 0.0
    open fun setCmd(value: Double) {}
    open fun setManual() {}
}

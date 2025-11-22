package mods.eln.transparentnode.thermaldissipator

import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.sim.ThermalLoad
import mods.eln.sim.nbt.NbtThermalLoad
import mods.eln.sim.process.destruct.ThermalLoadWatchDog
import mods.eln.sim.process.destruct.WorldExplosion
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNode

class ThermalDissipatorPassiveElement(
    node: TransparentNode?,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElement(node, descriptor) {

    val descriptor: ThermalDissipatorPassiveDescriptor = descriptor as ThermalDissipatorPassiveDescriptor
    private val thermalLoad = NbtThermalLoad("thermalLoad")
    private val thermalWatchdog = ThermalLoadWatchDog(thermalLoad)

    init {
        thermalLoadList.add(thermalLoad)
        slowProcessList.add(thermalWatchdog)

        thermalWatchdog
            .setMaximumTemperature(this.descriptor.warmLimit)
            .setDestroys(WorldExplosion(this).machineExplosion())
    }

    override fun getThermalLoad(side: Direction, lrdu: LRDU): ThermalLoad? {
        if (lrdu == LRDU.Down && side == Direction.YN) return thermalLoad
        return null
    }

    override fun initialize() {
        descriptor.applyTo(thermalLoad)
    }
}

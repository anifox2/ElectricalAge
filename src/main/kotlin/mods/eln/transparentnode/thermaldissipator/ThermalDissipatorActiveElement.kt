package mods.eln.transparentnode.thermaldissipator

import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.node.NodePeriodicPublishProcess
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sim.nbt.NbtThermalLoad
import mods.eln.sim.process.destruct.ThermalLoadWatchDog
import mods.eln.sim.process.destruct.WorldExplosion
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNode
import java.io.DataOutputStream

class ThermalDissipatorActiveElement(
    node: TransparentNode?,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElement(node, descriptor) {

    val descriptor: ThermalDissipatorActiveDescriptor = descriptor as ThermalDissipatorActiveDescriptor
    private val thermalLoad = NbtThermalLoad("thermalLoad")
    private val positiveLoad = NbtElectricalLoad("positiveLoad")
    val powerResistor = Resistor(positiveLoad, null)
    private val slowProcess = ThermalDissipatorActiveSlowProcess(this)
    private val thermalWatchdog = ThermalLoadWatchDog(thermalLoad)
    
    var lastPowerFactor = 0f

    override fun initialize() {
    }

    init {
        thermalLoadList.add(thermalLoad)
        electricalLoadList.add(positiveLoad)
        electricalComponentList.add(powerResistor)
        slowProcessList.add(slowProcess)
        slowProcessList.add(NodePeriodicPublishProcess(node, 4.0, 2.0))
        slowProcessList.add(thermalWatchdog)

        thermalWatchdog
            .setMaximumTemperature(this.descriptor.warmLimit)
            .setDestroys(WorldExplosion(this).machineExplosion())
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU): ElectricalLoad? {
        if (lrdu == LRDU.Down && side == Direction.YN) return positiveLoad
        return null
    }

    override fun getThermalLoad(side: Direction, lrdu: LRDU): ThermalLoad? {
        if (lrdu == LRDU.Down && side == Direction.YN) return thermalLoad
        return null
    }

    override fun initialize() {
        descriptor.applyTo(thermalLoad)
        descriptor.applyTo(positiveLoad)
    }

    override fun writeToPublishPacket(stream: DataOutputStream) {
        super.writeToPublishPacket(stream)
        stream.writeFloat(lastPowerFactor)
    }

    class ThermalDissipatorActiveSlowProcess(val dissipator: ThermalDissipatorActiveElement) : IProcess {
        override fun process(time: Double) {
            val descriptor = dissipator.descriptor
            val poweredFactor = dissipator.powerResistor.getPower() / descriptor.electricalNominalP
            val thermalRp = 1 / (1 / descriptor.thermalRp + poweredFactor / (descriptor.electricalToThermalRp))
            dissipator.thermalLoad.setRp(thermalRp)

            if (Math.abs(dissipator.lastPowerFactor - poweredFactor.toFloat()) > 0.2f) {
                dissipator.lastPowerFactor = poweredFactor.toFloat()
                dissipator.needPublish()
            }
        }
    }
}

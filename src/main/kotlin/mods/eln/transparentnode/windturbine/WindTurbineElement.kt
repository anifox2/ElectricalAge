package mods.eln.transparentnode.windturbine

import mods.eln.misc.Direction
import mods.eln.misc.INBTTReady
import mods.eln.misc.RcRcInterpolator
import mods.eln.misc.LRDU
import mods.eln.node.NodePeriodicPublishProcess
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.mna.component.PowerSource
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNode
import net.minecraft.nbt.CompoundTag

class WindTurbineElement(
    node: TransparentNode?,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElement(node, descriptor) {

    override val descriptor: WindTurbineDescriptor get() = transparentNodeDescriptor as WindTurbineDescriptor
    private val positiveLoad = NbtElectricalLoad("positiveLoad")
    val powerSource = PowerSource("powerSource", positiveLoad)
    private val slowProcess = WindTurbineSlowProcess(this)
    private var cableFront = Direction.ZP

    override fun initialize() {
    }

    init {
        electricalLoadList.add(positiveLoad)
        electricalComponentList.add(powerSource)
        if (node != null) {
            slowProcessList.add(NodePeriodicPublishProcess(node, 4.0, 4.0))
        }
        slowProcessList.add(slowProcess)
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU): ElectricalLoad? {
        if (lrdu != LRDU.Down) return null
        if (side == cableFront.left()) return positiveLoad
        return null
    }

    class WindTurbineSlowProcess(val turbine: WindTurbineElement) : IProcess, INBTTReady {
        private var refreshTimeout = 0.0
        private val refreshPeriode = 0.2
        private val filter = RcRcInterpolator(2f, 2f)

        override fun process(time: Double) {
            val d = turbine.descriptor
            refreshTimeout -= time
            if (refreshTimeout < 0) {
                refreshTimeout = refreshPeriode
                val windSpeed = getWind()
                if (windSpeed < 0) {
                    filter.setValue((filter.get() * (1 - 0.5f * time)).toFloat())
                } else {
                    filter.target = (windSpeed * d.nominalPower).toFloat()
                    filter.step(time.toFloat())
                }
                turbine.powerSource.setPower(filter.get().toDouble())
            }
        }

        private fun getWind(): Double {
            // TODO: Implement proper wind logic using WindProcess or similar
            return 1.0
        }

        override fun readFromNBT(nbt: CompoundTag, str: String) {
            val tag = nbt.getCompound(str)
            filter.setValue(tag.getFloat("filter"))
            filter.target = tag.getFloat("filterTarget")
        }

        override fun writeToNBT(nbt: CompoundTag, str: String) {
            val tag = CompoundTag()
            tag.putFloat("filter", filter.get())
            tag.putFloat("filterTarget", filter.target)
            nbt.put(str, tag)
        }
    }
}

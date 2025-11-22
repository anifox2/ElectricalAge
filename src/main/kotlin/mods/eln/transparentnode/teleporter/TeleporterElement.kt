package mods.eln.transparentnode.teleporter

import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.node.NodePeriodicPublishProcess
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNode
import java.util.ArrayList

class TeleporterElement(
    node: TransparentNode?,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElement(node, descriptor), ITeleporter {

    val descriptor: TeleporterDescriptor = descriptor as TeleporterDescriptor
    private val powerLoad = NbtElectricalLoad("powerLoad")
    val powerResistor = Resistor(powerLoad, null)
    private val slowProcess = TeleporterSlowProcess(this)

    companion object {
        val teleporterList = ArrayList<ITeleporter>()
    }

    override fun initialize() {
    }

    init {
        electricalLoadList.add(powerLoad)
        electricalComponentList.add(powerResistor)
        slowProcessList.add(slowProcess)
        slowProcessList.add(NodePeriodicPublishProcess(node, 2.0, 2.0))
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU): ElectricalLoad? {
        // Logic to return load based on connection
        return powerLoad
    }

    override fun getTeleportCoordonate(): Coordinate {
        return node.coordinate
    }

    override fun getName(): String {
        return "Teleporter"
    }

    override fun reservate(): Boolean {
        return true
    }

    override fun reservateRefresh(doorState: Boolean, processRatio: Float) {
        // Update state
    }

    override fun onBreakFromWorld() {
        super.onBreakFromWorld()
        teleporterList.remove(this)
    }

    override fun initialize() {
        teleporterList.add(this)
    }

    class TeleporterSlowProcess(val teleporter: TeleporterElement) : IProcess {
        override fun process(time: Double) {
            // Teleportation logic
        }
    }
}

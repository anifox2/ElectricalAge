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

    override val descriptor: TeleporterDescriptor
        get() = transparentNodeDescriptor as TeleporterDescriptor
    private val powerLoad = NbtElectricalLoad("powerLoad")
    val powerResistor = Resistor(powerLoad, null)
    private val slowProcess = TeleporterSlowProcess(this)

    companion object {
        val teleporterList = ArrayList<ITeleporter>()
    }

    init {
        electricalLoadList.add(powerLoad)
        electricalComponentList.add(powerResistor)
        slowProcessList.add(slowProcess)
        slowProcessList.add(NodePeriodicPublishProcess(node!!, 2.0, 2.0))
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU): ElectricalLoad? {
        // Logic to return load based on connection
        return powerLoad
    }

    override fun getTeleportCoordonate(): Coordinate {
        return node!!.coordinate
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

    fun onBreakFromWorld() {
        //super.onBreakFromWorld()
        teleporterList.remove(this)
    }

    override fun initialize() {
        teleporterList.add(this)
    }

    class TeleporterSlowProcess(val teleporter: TeleporterElement) : IProcess {
        override fun process(time: Double) {
            if (teleporter.node == null) return
            val level = teleporter.node!!.coordinate.world() ?: return
            val pos = teleporter.node!!.coordinate.toBlockPos()
            
            // Check for entities in the block space
            val aabb = net.minecraft.world.phys.AABB(pos)
            val entities = level.getEntitiesOfClass(net.minecraft.world.entity.Entity::class.java, aabb)
            
            if (entities.isNotEmpty()) {
                val dest = findDestination(teleporter)
                if (dest != null) {
                    val destCoord = dest.getTeleportCoordonate()
                    for (entity in entities) {
                        if (!entity.isPassenger && !entity.isVehicle) {
                            // TODO: Handle dimension change if needed
                            if (destCoord.dimension == teleporter.node!!.coordinate.dimension) {
                                entity.teleportTo(destCoord.x + 0.5, destCoord.y + 1.0, destCoord.z + 0.5)
                            }
                        }
                    }
                }
            }
        }

        fun findDestination(source: TeleporterElement): ITeleporter? {
            val myName = source.getName()
            for (t in TeleporterElement.teleporterList) {
                if (t !== source && t.getName() == myName) return t
            }
            return null
        }
    }
}

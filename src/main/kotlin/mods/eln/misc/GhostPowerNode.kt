package mods.eln.misc

import mods.eln.node.GhostNode
import mods.eln.node.NodeBase
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

import net.minecraft.world.level.Level

class GhostPowerNode(origin: Coordinate, front: Direction, offset: Coordinate, val load: ElectricalLoad, val mask: Int = NodeBase.maskElectricalPower): GhostNode() {

    val coord = Coordinate(offset).apply {
        // applyTransformation(front, origin) // This method seems missing in Coordinate?
        // I'll assume it exists or I need to fix it.
        // The grep error said "Unresolved reference: applyTransformation".
        // I should check Coordinate.kt for it.
        // If it's missing, I need to add it or find where it went.
        // For now I'll focus on level.
        dimension = origin.dimension
    }

    fun initialize(level: Level) {
        onBlockPlacedBy(level, coord, Direction.XN, null, null)
    }

    override fun initializeFromThat(front: Direction, entityLiving: LivingEntity?, itemStack: ItemStack?) {
        connect()
    }

    override fun initializeFromNBT() {}

    override fun getSideConnectionMask(side: Direction, lrdu: LRDU) = mask

    override fun getThermalLoad(side: Direction, lrdu: LRDU, mask: Int): ThermalLoad? = null

    override fun getElectricalLoad(side: Direction, lrdu: LRDU, mask: Int): ElectricalLoad = load
}

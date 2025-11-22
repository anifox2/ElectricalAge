package mods.eln.simplenode

import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.node.simple.SimpleNode
import mods.eln.node.simple.SimpleNodeBlock
import mods.eln.node.simple.SimpleNodeEntity
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.Level

class ConduitBlock(): SimpleNodeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return ConduitEntity(pos, state)
    }

    override fun newNode(): SimpleNode {
        return ConduitNode()
    }
}

class ConduitNode: SimpleNode() {

    override fun initialize() {
        connect()
    }

    override val nodeUuid: String
        get() = getNodeUuidStatic()

    override fun getSideConnectionMask(side: Direction, lrdu: LRDU): Int {
        return maskConduit
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU, mask: Int): ElectricalLoad? {
        return null
    }
    
    override fun getThermalLoad(side: Direction, lrdu: LRDU, mask: Int): ThermalLoad? {
        return null
    }

    companion object {
        fun getNodeUuidStatic(): String {
            return "ElnConduit"
        }
    }
}

class ConduitEntity(pos: BlockPos, state: BlockState): SimpleNodeEntity(null!!, pos, state, ConduitNode.getNodeUuidStatic()) {
}

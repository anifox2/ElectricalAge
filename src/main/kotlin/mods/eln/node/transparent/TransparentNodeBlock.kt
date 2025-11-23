package mods.eln.node.transparent

import mods.eln.Eln
import mods.eln.init.Registration
import mods.eln.node.NodeBase
import mods.eln.node.NodeBlock
import mods.eln.node.NodeBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.HitResult

class TransparentNodeBlock(properties: Properties) : NodeBlock(properties, { pos, state -> TransparentNodeBlockEntity(pos, state) }, 0) {

    override fun getCloneItemStack(state: BlockState, target: HitResult, level: BlockGetter, pos: BlockPos, player: Player): ItemStack {
        val entity = level.getBlockEntity(pos) as? TransparentNodeBlockEntity
        if (entity != null) {
             val stack = ItemStack(this)
             stack.damageValue = entity.elementRenderId.toInt()
             return stack
        }
        return super.getCloneItemStack(state, target, level, pos, player)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        // TODO: Implement custom shapes based on TransparentNodeBlockEntity descriptor
        return Shapes.block()
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player) {
        if (!level.isClientSide) {
            val entity = level.getBlockEntity(pos) as? NodeBlockEntity
            if (entity != null) {
                val nodeBase: NodeBase? = entity.node
                if (nodeBase is TransparentNode) {
                    nodeBase.removedByPlayer = player as? net.minecraft.server.level.ServerPlayer
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player)
    }
}


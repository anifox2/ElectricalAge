package mods.eln.node

import mods.eln.misc.Direction
import mods.eln.misc.Direction.Companion.fromIntMinecraftSide
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

abstract class NodeBlock(properties: Properties, val blockEntityFactory: (BlockPos, BlockState) -> BlockEntity, blockItemNbr: Int) : Block(properties), EntityBlock {

    var blockItemNbr: Int

    override fun getSignal(state: BlockState, level: BlockGetter, pos: BlockPos, direction: net.minecraft.core.Direction): Int {
        val entity = level.getBlockEntity(pos) as? NodeBlockEntity ?: return 0
        return entity.isProvidingWeakPower(fromIntMinecraftSide(direction.ordinal))
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        val tileEntity = level.getBlockEntity(pos) as? NodeBlockEntity
        tileEntity?.onBlockPlacedBy(null, placer, stack)
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        if (!level.isClientSide) {
            val entity = level.getBlockEntity(pos) as? NodeBlockEntity
            entity?.onBlockAdded()
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (!state.`is`(newState.block)) {
            val entity = level.getBlockEntity(pos) as? NodeBlockEntity
            entity?.onBreakBlock()
            super.onRemove(state, level, pos, newState, isMoving)
        }
    }

    override fun neighborChanged(state: BlockState, level: Level, pos: BlockPos, block: Block, fromPos: BlockPos, isMoving: Boolean) {
        if (!level.isClientSide) {
            val entity = level.getBlockEntity(pos) as? NodeBlockEntity
            entity?.onNeighborBlockChange()
        }
    }

    override fun use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS
        val entity = level.getBlockEntity(pos) as? NodeBlockEntity ?: return InteractionResult.PASS
        val side = hit.direction
        val vec = hit.location.subtract(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
        return if (entity.onBlockActivated(player, fromIntMinecraftSide(side.ordinal), vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat())) InteractionResult.SUCCESS else InteractionResult.PASS
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return blockEntityFactory(pos, state)
    }

    init {
        this.blockItemNbr = blockItemNbr
    }
}

package mods.eln.node.simple

import mods.eln.misc.Direction
import mods.eln.misc.Direction.Companion.fromIntMinecraftSide
import mods.eln.misc.Utils.entityLivingViewDirection
import mods.eln.misc.Utils.isRemote
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.BlockHitResult

abstract class SimpleNodeBlock protected constructor(properties: BlockBehaviour.Properties) : Block(properties), EntityBlock {
    var descriptorKey: String? = null
    fun setDescriptorKey(descriptorKey: String?): SimpleNodeBlock {
        this.descriptorKey = descriptorKey
        return this
    }

    fun setDescriptor(descriptor: DescriptorBase): SimpleNodeBlock {
        descriptorKey = descriptor.descriptorKey
        return this
    }

    fun getFrontForPlacement(e: LivingEntity?): Direction {
        return entityLivingViewDirection(e!!).inverse()
    }

    abstract fun newNode(): SimpleNode?

    fun getNode(world: Level, pos: BlockPos): SimpleNode? {
        val entity = world.getBlockEntity(pos) as? SimpleNodeEntity
        return entity?.node
    }

    fun getEntity(world: Level, pos: BlockPos): SimpleNodeEntity {
        return world.getBlockEntity(pos) as SimpleNodeEntity
    }

    override fun playerWillDestroy(world: Level, pos: BlockPos, state: BlockState, player: Player) {
        if (!world.isClientSide) {
            val node = getNode(world, pos)
            if (node != null && player is ServerPlayer) {
                node.removedByPlayer = player
            }
        }
        super.playerWillDestroy(world, pos, state, player)
    }

    override fun onPlace(state: BlockState, world: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        if (!world.isClientSide && state.block != oldState.block) {
            val entity = world.getBlockEntity(pos) as? SimpleNodeEntity
            entity?.onBlockAdded()
        }
    }

    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (state.block != newState.block) {
            val entity = world.getBlockEntity(pos) as? SimpleNodeEntity
            entity?.onBreakBlock()
            super.onRemove(state, world, pos, newState, isMoving)
        }
    }

    override fun neighborChanged(state: BlockState, world: Level, pos: BlockPos, block: Block, fromPos: BlockPos, isMoving: Boolean) {
        if (!world.isClientSide) {
            val entity = world.getBlockEntity(pos) as? SimpleNodeEntity
            entity?.onNeighborBlockChange()
        }
    }

    override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: net.minecraft.world.item.ItemStack) {
        if (!world.isClientSide) {
            val entity = world.getBlockEntity(pos) as? SimpleNodeEntity
            if (entity != null) {
                var node = entity.node
                if (node == null) {
                    node = newNode()
                    if (node != null) {
                        node.coordinate = mods.eln.misc.Coordinate(pos, world)
                        node.level = world
                        mods.eln.node.NodeManager.instance!!.addNode(node)
                        entity.node = node
                    }
                }
                
                if (node != null) {
                    node.descriptorKey = this.descriptorKey
                    node.onBlockPlacedBy(world, mods.eln.misc.Coordinate(pos, world), getFrontForPlacement(placer), placer, stack)
                }
            }
        }
    }

    override fun use(state: BlockState, world: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        val entity = world.getBlockEntity(pos) as? SimpleNodeEntity
        val side = hit.direction.ordinal
        val vx = (hit.location.x - pos.x).toFloat()
        val vy = (hit.location.y - pos.y).toFloat()
        val vz = (hit.location.z - pos.z).toFloat()
        
        if (entity != null && entity.onBlockActivated(player, fromIntMinecraftSide(side), vx, vy, vz)) {
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }
}

package mods.eln.node.six

import mods.eln.Eln
import mods.eln.misc.Direction
import mods.eln.misc.Direction.Companion.fromIntMinecraftSide
import mods.eln.misc.Utils
import mods.eln.misc.Utils.println
import mods.eln.misc.Utils.updateAllLightTypes
import mods.eln.misc.Utils.updateSkylight
import mods.eln.node.NodeBase
import mods.eln.node.NodeBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.*

class SixNodeBlock(properties: Properties) : NodeBlock(properties, { pos, state -> SixNodeEntity(pos, state) }, 0) {

    override fun getCloneItemStack(state: BlockState, target: HitResult, level: BlockGetter, pos: BlockPos, player: Player): ItemStack {
        val entity = level.getBlockEntity(pos) as? SixNodeEntity
        if (entity != null && target is BlockHitResult) {
            val render = entity.elementRenderList[fromIntMinecraftSide(target.direction.ordinal)!!.int]
            if (render != null) {
                return render.sixNodeDescriptor.newItemStack()
            }
        }
        return super.getCloneItemStack(state, target, level, pos, player)
    }

    override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        if (nodeHasCache(level, pos) || hasVolume(level, pos)) {
             return Shapes.block()
        }
        return Shapes.empty()
    }

    fun hasVolume(level: BlockGetter, pos: BlockPos): Boolean {
        val entity = getEntity(level, pos) ?: return false
        return entity.hasVolume(level, pos)
    }

    /*
    override fun getDestroySpeed(state: BlockState, level: BlockGetter, pos: BlockPos): Float {
        return 0.3f
    }
    */

    fun getEntity(level: BlockGetter, pos: BlockPos): SixNodeEntity? {
        val tileEntity = level.getBlockEntity(pos)
        if (tileEntity != null && tileEntity is SixNodeEntity) return tileEntity
        return null
    }

    override fun useShapeForLightOcclusion(state: BlockState): Boolean {
        return false
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.INVISIBLE
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        // super.setPlacedBy(level, pos, state, placer, stack)
    }

    override fun onDestroyedByPlayer(state: BlockState, level: Level, pos: BlockPos, player: Player, willHarvest: Boolean, fluid: net.minecraft.world.level.material.FluidState): Boolean {
        if (level.isClientSide) return false
        val tileEntity = level.getBlockEntity(pos) as? SixNodeEntity ?: return false
        
        val start = player.eyePosition
        val look = player.lookAngle
        val end = start.add(look.x * 5.0, look.y * 5.0, look.z * 5.0)
        val hit = level.clip(ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player))

        if (hit.type == HitResult.Type.MISS) return false
        
        val sideHit = hit.direction
        
        val sixNode = tileEntity.node as SixNode? ?: return true
        if (sixNode.sixNodeCacheBlock !== Blocks.AIR) {
            if (!player.isCreative) {
                 // val stack = ItemStack(sixNode.sixNodeCacheBlock) 
                 // sixNode.dropItem(stack)
            }
            sixNode.sixNodeCacheBlock = Blocks.AIR
            updateAllLightTypes(level, pos)
            sixNode.needPublish = true
            return false
        }
        
        if (false == sixNode.playerAskToBreakSubBlock(player as? net.minecraft.server.level.ServerPlayer, fromIntMinecraftSide(sideHit.ordinal)!!)) return false
        
        return if (sixNode.ifSideRemain) true else super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid)
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (state.block != newState.block) {
             if (!level.isClientSide) {
                val tileEntity = level.getBlockEntity(pos) as? SixNodeEntity
                val sixNode = tileEntity?.node as SixNode?
                if (sixNode != null) {
                    for (direction in Direction.values()) {
                        if (sixNode.getSideEnable(direction)) {
                            sixNode.deleteSubBlock(null, direction)
                        }
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving)
        }
    }

    override fun neighborChanged(state: BlockState, level: Level, pos: BlockPos, block: Block, fromPos: BlockPos, isMoving: Boolean) {
        val tileEntity = level.getBlockEntity(pos) as? SixNodeEntity
        val sixNode = tileEntity?.node as SixNode? ?: return
        for (direction in Direction.values()) {
            if (sixNode.getSideEnable(direction)) {
                if (!getIfOtherBlockIsSolid(level, pos, direction)) {
                    sixNode.deleteSubBlock(null, direction)
                }
            }
        }
        if (!sixNode.ifSideRemain) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
        } else {
            super.neighborChanged(state, level, pos, block, fromPos, isMoving)
        }
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return Shapes.block() 
    }

    fun getIfOtherBlockIsSolid(level: Level, pos: BlockPos, direction: Direction): Boolean {
        val neighborPos = pos.relative(direction.toMCDirection())
        val state = level.getBlockState(neighborPos)
        return !state.isAir 
    }

    fun nodeHasCache(level: BlockGetter, pos: BlockPos): Boolean {
        val tileEntity = level.getBlockEntity(pos) as? SixNodeEntity
        if (tileEntity != null) {
             return tileEntity.sixNodeCacheBlock !== Blocks.AIR
        }
        return false
    }
    
    override fun getLightEmission(state: BlockState, level: BlockGetter, pos: BlockPos): Int {
         return 0
    }
    
    val nodeUuid: String
        get() = "s"

    companion object {
        fun isIn(value: Double, min: Double, max: Double): Boolean {
            return if (value >= min && value <= max) true else false
        }
    }
}


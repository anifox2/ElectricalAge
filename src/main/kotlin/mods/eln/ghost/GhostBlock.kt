@file:Suppress("NAME_SHADOWING", "DEPRECATION", "OVERRIDE_DEPRECATION")
package mods.eln.ghost

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction.Companion.fromIntMinecraftSide
import mods.eln.node.transparent.TransparentNodeBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.util.StringRepresentable
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class GhostBlock(props: BlockBehaviour.Properties) : Block(props) {

    enum class GhostType(val id: Int) : StringRepresentable {
        CUBE(0),
        FLOOR(1),
        LADDER(2);

        override fun getSerializedName(): String = name.lowercase()
        
        companion object {
            fun fromId(id: Int): GhostType = values().find { it.id == id } ?: CUBE
        }
    }

    companion object {
        val TYPE: EnumProperty<GhostType> = EnumProperty.create("type", GhostType::class.java)
        val FLOOR_SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0)
        
        const val tCube = 0
        const val tFloor = 1
        const val tLadder = 2

        fun defaultProperties(): BlockBehaviour.Properties =
            BlockBehaviour.Properties.of()
                .strength(0.5f)
                .noOcclusion()
                .noCollission() // Default to no collision, override in getCollisionShape
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(TYPE, GhostType.CUBE))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(TYPE)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return when (state.getValue(TYPE)) {
            GhostType.FLOOR -> FLOOR_SHAPE
            GhostType.LADDER -> Shapes.empty() // Or a thin box if you want selection
            GhostType.CUBE -> delegateShapeFromTransparentNode(state, level, pos)
        }
    }

    override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return when (state.getValue(TYPE)) {
            GhostType.FLOOR -> FLOOR_SHAPE
            GhostType.LADDER -> Shapes.empty()
            GhostType.CUBE -> delegateShapeFromTransparentNode(state, level, pos)
        }
    }

    private fun delegateShapeFromTransparentNode(state: BlockState, level: BlockGetter, pos: BlockPos): VoxelShape {
        // This needs to access the underlying TransparentNodeBlockEntity to get collision boxes
        // Since we don't have easy access to the old addCollisionBoxesToList logic directly mapped to VoxelShape yet,
        // we might need to approximate or fetch from the element.
        
        // For now, returning a full cube or empty depending on context might be safer until TransparentNodeBlockEntity is ported.
        // But the original code did:
        /*
        val element = getElement(world, x, y, z)
        val coord = if (element == null) null else element.observatorCoordonate
        val te = coord?.tileEntity
        if (te != null && te is TransparentNodeBlockEntity) {
            te.addCollisionBoxesToList(par5AABB, list, element!!.elementCoordinate)
        }
        */
        
        // We can try to get the element
        if (level is Level) { // getElement needs Level
             val element = getElement(level, pos)
             // If we can get the shape from element, great. 
             // For now, let's return a full cube if it's a CUBE type, or empty if we can't find it.
             // Ideally TransparentNodeBlockEntity should expose a getShape(elementCoordinate) method.
             return Shapes.block() 
        }
        return Shapes.block()
    }

    override fun isLadder(state: BlockState, level: LevelReader, pos: BlockPos, entity: LivingEntity): Boolean {
        return state.getValue(TYPE) == GhostType.LADDER
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.INVISIBLE
    }

    override fun getCloneItemStack(state: BlockState, target: HitResult, level: BlockGetter, pos: BlockPos, player: Player): ItemStack {
        return ItemStack.EMPTY
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (!level.isClientSide && !state.`is`(newState.block)) {
            val element = getElement(level, pos)
            element?.breakBlock()
        }
        super.onRemove(state, level, pos, newState, isMoving)
    }

    override fun use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        if (!level.isClientSide) {
            val element = getElement(level, pos)
            if (element != null) {
                val side = hit.direction
                val localHit = hit.location.subtract(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                val handled = element.onBlockActivated(player, fromIntMinecraftSide(side.ordinal), localHit.x.toFloat(), localHit.y.toFloat(), localHit.z.toFloat())
                if (handled) return InteractionResult.CONSUME
            }
        }
        return InteractionResult.SUCCESS
    }

    fun getElement(level: Level, pos: BlockPos): GhostElement? {
        return Eln.ghostManager?.getGhost(Coordinate(pos.x, pos.y, pos.z, level))
    }

    val nodeUuid: String
        get() = "g"
}


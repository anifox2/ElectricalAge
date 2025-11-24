@file:Suppress("NAME_SHADOWING")
package mods.eln.node.transparent

import mods.eln.generic.GenericItemBlockUsingDamage
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.Utils.addChatMessage
import mods.eln.misc.Utils.nullCheck
import mods.eln.node.NodeBlock
import net.minecraft.world.level.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.InteractionResult
import net.minecraft.core.BlockPos
import java.util.function.Consumer
import net.minecraftforge.client.extensions.common.IClientItemExtensions
import mods.eln.client.ElnItemRenderer

class TransparentNodeItem(b: Block?) : GenericItemBlockUsingDamage<TransparentNodeDescriptor>(b!!) {
    
    override fun initializeClient(consumer: Consumer<IClientItemExtensions>) {
        consumer.accept(object : IClientItemExtensions {
            override fun getCustomRenderer(): net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer {
                return ElnItemRenderer.instance
            }
        })
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val player = context.player ?: return InteractionResult.FAIL
        val pos = context.clickedPos
        val side = context.clickedFace
        val stack = context.itemInHand

        if (level.isClientSide) return InteractionResult.SUCCESS

        var startPos = pos
        val state = level.getBlockState(pos)
        if (!state.canBeReplaced(net.minecraft.world.item.context.BlockPlaceContext(context))) {
            startPos = pos.relative(side)
        }

        val descriptor = getDescriptor(stack) ?: return InteractionResult.FAIL
        val direction = Direction.fromIntMinecraftSide(side.ordinal)!!.inverse()
        val front = descriptor.getFrontFromPlace(direction, player) ?: return InteractionResult.FAIL

        var x = startPos.x
        var y = startPos.y
        var z = startPos.z

        val v = intArrayOf(descriptor.spawnDeltaX, descriptor.spawnDeltaY, descriptor.spawnDeltaZ)
        val offset = front.rotateFromXN(net.minecraft.world.phys.Vec3(v[0].toDouble(), v[1].toDouble(), v[2].toDouble()))
        x += offset.x.toInt()
        y += offset.y.toInt()
        z += offset.z.toInt()

        val targetPos = BlockPos(x, y, z)
        val coord = Coordinate(x, y, z, level)

        var error: String? = null
        if (descriptor.checkCanPlace(coord, front, level).also { error = it } != null) {
            addChatMessage(player, error!!)
            return InteractionResult.FAIL
        }

        val newState = this.block.defaultBlockState()
        if (level.setBlock(targetPos, newState, 3)) {
            val entity = level.getBlockEntity(targetPos) as? TransparentNodeBlockEntity

            if (entity != null) {
                entity.onBlockPlacedBy(front, player, stack)

                if (!player.isCreative) stack.shrink(1)
                return InteractionResult.SUCCESS
            }
        }

        return InteractionResult.FAIL
    }

    init {
        // setHasSubtypes(true) // Handled by Item properties
        // unlocalizedName = "TransparentNodeItem"
    }
}

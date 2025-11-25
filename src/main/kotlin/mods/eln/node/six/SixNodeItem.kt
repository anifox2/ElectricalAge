@file:Suppress("NAME_SHADOWING")
package mods.eln.node.six

import mods.eln.Eln
import mods.eln.generic.GenericItemBlockUsingDamage
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction.Companion.fromIntMinecraftSide
import mods.eln.misc.LRDU
import mods.eln.misc.Utils.addChatMessage
import net.minecraft.world.level.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.function.Consumer
import net.minecraftforge.client.extensions.common.IClientItemExtensions
import mods.eln.client.ElnItemRenderer

class SixNodeItem(b: Block?) : GenericItemBlockUsingDamage<SixNodeDescriptor>(b!!) {
    override fun initializeClient(consumer: Consumer<IClientItemExtensions>) {
        consumer.accept(object : IClientItemExtensions {
            override fun getCustomRenderer(): net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer {
                return ElnItemRenderer.instance
            }
        })
    }

    override fun useOn(context: net.minecraft.world.item.context.UseOnContext): net.minecraft.world.InteractionResult {
        val level = context.level
        val player = context.player ?: return net.minecraft.world.InteractionResult.FAIL
        val stack = context.itemInHand
        val pos = context.clickedPos
        val side = context.clickedFace

        if (level.isClientSide) return net.minecraft.world.InteractionResult.SUCCESS

        val descriptor = getDescriptor(stack) ?: return net.minecraft.world.InteractionResult.FAIL

        // Determine target position
        var targetPos = pos
        val state = level.getBlockState(pos)
        val block = state.block

        var isAddingToExisting = false

        if (block is SixNodeBlock) {
            isAddingToExisting = true
        } else if (!state.canBeReplaced(net.minecraft.world.item.context.BlockPlaceContext(context))) {
            targetPos = pos.relative(side)
            val targetState = level.getBlockState(targetPos)
            if (targetState.block is SixNodeBlock) {
                isAddingToExisting = true
            }
        }

        // Check if we can place
        var direction = fromIntMinecraftSide(side.ordinal)!!
        if (!isAddingToExisting) {
             direction = direction.inverse()
        }
        val coord = Coordinate(targetPos.x, targetPos.y, targetPos.z, level)

        var error: String? = null
        if (descriptor.checkCanPlace(coord, direction, LRDU.Up).also { error = it } != null) {
            addChatMessage(player, error!!)
            return net.minecraft.world.InteractionResult.FAIL
        }

        // Place logic
        if (isAddingToExisting) {
            val entity = level.getBlockEntity(targetPos) as? SixNodeEntity
            val sixNode = entity?.node as? SixNode

            if (sixNode != null) {
                // Check if side is free
                if (!sixNode.getSideEnable(direction)) {
                    // Add sub block
                    sixNode.createSubBlock(stack, direction, player)
                    level.sendBlockUpdated(targetPos, level.getBlockState(targetPos), level.getBlockState(targetPos), 3)

                    if (!player.isCreative) stack.shrink(1)
                    return net.minecraft.world.InteractionResult.SUCCESS
                }
            }
        } else {
            // Place new block
            val targetState = level.getBlockState(targetPos)
            if (targetState.isAir || targetState.canBeReplaced(net.minecraft.world.item.context.BlockPlaceContext(context))) {
                val sixNodeBlock = this.block as SixNodeBlock
                if (sixNodeBlock.getIfOtherBlockIsSolid(level, targetPos, direction)) {
                    // Place block
                    val newState = this.block.defaultBlockState()
                    if (level.setBlock(targetPos, newState, 3)) {
                        val entity = level.getBlockEntity(targetPos) as? SixNodeEntity

                        if (entity != null) {
                            entity.onBlockPlacedBy(direction, player, stack)
                            val sixNode = entity.node as? SixNode

                            if (sixNode != null) {
                                sixNode.createSubBlock(stack, direction, player)

                                if (!player.isCreative) stack.shrink(1)
                                return net.minecraft.world.InteractionResult.SUCCESS
                            }
                        }
                    }
                }
            }
        }

        return net.minecraft.world.InteractionResult.FAIL
    }
    init {
        //setHasSubtypes(true)
        //unlocalizedName = "SixNodeItem"
    }
}

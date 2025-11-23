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

class TransparentNodeItem(b: Block?) : GenericItemBlockUsingDamage<TransparentNodeDescriptor>(b!!) {
    
    override fun useOn(context: UseOnContext): InteractionResult {
        /*
        val world = context.level
        val player = context.player ?: return InteractionResult.FAIL
        val pos = context.clickedPos
        val side = context.clickedFace
        val stack = context.itemInHand
        
        if (world.isRemote) return InteractionResult.SUCCESS // Client side prediction? Or just consume?

        var x = pos.x
        var y = pos.y
        var z = pos.z
        
        // Logic to adjust position based on side (like standard placement)
        // But TransparentNodeItem logic seems to do it manually?
        // "x += v[0]" etc.
        
        val descriptor = getDescriptor(stack) ?: return InteractionResult.FAIL
        val direction = Direction.fromMCDirection(side).inverse()
        val front = descriptor.getFrontFromPlace(direction, player) ?: return InteractionResult.FAIL
        
        val v = intArrayOf(descriptor.spawnDeltaX, descriptor.spawnDeltaY, descriptor.spawnDeltaZ)
        front.rotateFromXN(net.minecraft.world.phys.Vec3(v[0].toDouble(), v[1].toDouble(), v[2].toDouble())).let {
            x += it.x.toInt()
            y += it.y.toInt()
            z += it.z.toInt()
        }
        
        val coord = Coordinate(x, y, z, 0) // TODO: Dimension
        var error: String? = null
        
        if (descriptor.checkCanPlace(coord, front, world).also { error = it } != null) {
            addChatMessage(player, error!!)
            return InteractionResult.FAIL
        }
        
        val ghostgroup = descriptor.getGhostGroupFront(front)
        ghostgroup?.plot(coord, coord, descriptor.ghostGroupUuid)
        
        val node = TransparentNode()
        // node.onBlockPlacedBy(coord, front, player, stack) // This needs to be called after placement or set up
        
        // Manual placement
        val placePos = BlockPos(x, y, z)
        // We need to set the block. 
        // But TransparentNodeItem is a BlockItem, so we should use super.place?
        // But super.place does standard placement. This item seems to have custom offset logic.
        
        // If we manually set block, we bypass some checks but that's what the old code did.
        // world.setBlock(x, y, z, Block.getBlockFromItem(this), node.blockMetadata, 0x03)
        
        // We need the block state.
        val state = this.block.defaultBlockState() // Assuming block is set
        if (world.setBlock(placePos, state, 3)) {
             // Handle TileEntity/Node setup
             // ...
             return InteractionResult.SUCCESS
        }
        */
        return InteractionResult.FAIL
    }

    init {
        // setHasSubtypes(true) // Handled by Item properties
        // unlocalizedName = "TransparentNodeItem"
    }
}

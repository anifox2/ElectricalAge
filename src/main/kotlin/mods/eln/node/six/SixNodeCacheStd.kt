package mods.eln.node.six

import mods.eln.misc.Utils
import mods.eln.node.ISixNodeCache
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.item.ItemStack

class SixNodeCacheStd : ISixNodeCache {
    override fun accept(stack: ItemStack): Boolean {
        Utils.println("Testing item ${stack.displayName} for blockiness")
        val b = Block.byItem(stack.item)
        if (b == net.minecraft.world.level.block.Blocks.AIR) return false
        if (b is EntityBlock) return false
        // Utils.println("Item is probably a block with render type ${b.defaultBlockState().renderShape}")
        return if (stack.item is SixNodeItem) false else b.defaultBlockState().renderShape == RenderShape.MODEL
    }

    override fun getMeta(stack: ItemStack): Int {
        return stack.damageValue
    }
}

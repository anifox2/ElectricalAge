package mods.eln.node.six

import mods.eln.misc.Utils
import mods.eln.node.ISixNodeCache
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.BlockContainer
import net.minecraft.world.item.ItemStack

class SixNodeCacheStd : ISixNodeCache {
    override fun accept(stack: ItemStack): Boolean {
        Utils.println("Testing item ${stack.displayName} for blockiness")
        val b = Block.getBlockFromItem(stack.item) ?: return false
        if (b is BlockContainer) return false
        Utils.println("Item is probably a block with render type ${b.renderType}")
        return if (stack.item is SixNodeItem) false else when (b.renderType) {
            0 -> true
            31 -> true // Logs
            39 -> true // Quartz block
            59 -> true // Chisel?
            else -> false
        }
    }

    override fun getMeta(stack: ItemStack): Int {
        return stack.itemDamage
    }
}

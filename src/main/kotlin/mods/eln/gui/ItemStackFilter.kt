package mods.eln.gui

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class ItemStackFilter(var item: Item) : IItemStackFilter {
    override fun tryItemStack(stack: ItemStack): Boolean {
        return stack.item == item
    }
}

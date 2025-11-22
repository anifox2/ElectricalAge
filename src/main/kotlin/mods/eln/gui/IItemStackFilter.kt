package mods.eln.gui

import net.minecraft.world.item.ItemStack

interface IItemStackFilter {
    fun tryItemStack(stack: ItemStack): Boolean
}

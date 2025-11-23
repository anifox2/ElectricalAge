package mods.eln.gui

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.tags.ItemTags
import net.minecraft.resources.ResourceLocation

class ItemStackFilter(var item: Item) : IItemStackFilter {
    override fun tryItemStack(stack: ItemStack): Boolean {
        return stack.item == item
    }

    companion object {
        @JvmStatic
        fun OreDict(name: String): IItemStackFilter {
            return object : IItemStackFilter {
                override fun tryItemStack(stack: ItemStack): Boolean {
                    if (name == "dustCoal") {
                         val tag = ItemTags.create(ResourceLocation("forge", "dusts/coal"))
                         return stack.`is`(tag)
                    }
                    return false
                }
            }
        }
    }
}

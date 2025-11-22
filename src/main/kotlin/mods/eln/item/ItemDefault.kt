package mods.eln.item

import mods.eln.gui.GuiVerticalExtender
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class ItemDefault : Item(Properties()) {
    interface IPlugIn {
        fun top(y: Int, extender: GuiVerticalExtender, stack: ItemStack): Int
        fun bottom(y: Int, extender: GuiVerticalExtender, stack: ItemStack): Int
    }
}

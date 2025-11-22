package mods.eln.gui

import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

class SlotFilter(
    inventory: Container,
    slot: Int,
    x: Int,
    y: Int,
    var stackLimit: Int,
    var itemStackFilter: Array<IItemStackFilter>,
    skin: ISlotSkin.SlotSkin,
    comment: Array<String>
) : SlotWithSkinAndComment(inventory, slot, x, y, skin, comment) {

    override fun mayPlace(stack: ItemStack): Boolean {
        for (filter in itemStackFilter) {
            if (filter.tryItemStack(stack)) return true
        }
        return false
    }

    override fun getMaxStackSize(): Int {
        return stackLimit
    }
}

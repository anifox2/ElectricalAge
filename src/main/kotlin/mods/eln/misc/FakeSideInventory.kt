package mods.eln.misc

import net.minecraft.world.entity.player.Player
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Direction

class FakeSideInventory : WorldlyContainer {
    override fun getContainerSize(): Int {
        return 0
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun getItem(var1: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeItem(var1: Int, var2: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeItemNoUpdate(var1: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun setItem(var1: Int, var2: ItemStack) {
    }

    override fun getMaxStackSize(): Int {
        return 0
    }

    override fun setChanged() {
    }

    override fun stillValid(var1: Player): Boolean {
        return false
    }

    override fun startOpen(player: Player) {
    }

    override fun stopOpen(player: Player) {
    }

    override fun canPlaceItem(var1: Int, var2: ItemStack): Boolean {
        return false
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        return IntArray(0)
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean {
        return false
    }

    override fun canTakeItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction): Boolean {
        return false
    }

    override fun clearContent() {
    }

    companion object {
        var instance: FakeSideInventory = FakeSideInventory()
    }
}
        return null
    }

    override fun setItem(var1: Int, var2: ItemStack) {}
    override fun getInventoryName(): String {
        return "FakeSideInventory"
    }

    override fun hasCustomInventoryName(): Boolean {
        return false
    }

    override fun getMaxStackSize(): Int {
        return 0
    }

    override fun setChanged() {}
    override fun stillValid(var1: Player): Boolean {
        return false
    }

    override fun startOpen() {}
    override fun stopOpen() {}
    override fun canPlaceItem(var1: Int, var2: ItemStack): Boolean {
        return false
    }

    override fun getAccessibleSlotsFromSide(var1: Int): IntArray {
        return intArrayOf()
    }

    override fun canInsertItem(var1: Int, var2: ItemStack, var3: Int): Boolean {
        return false
    }

    override fun canExtractItem(var1: Int, var2: ItemStack, var3: Int): Boolean {
        return false
    }

    companion object {
        @JvmStatic
        val instance = FakeSideInventory()
    }
}

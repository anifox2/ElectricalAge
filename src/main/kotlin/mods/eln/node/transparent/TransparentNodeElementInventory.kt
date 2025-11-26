package mods.eln.node.transparent

import mods.eln.misc.INBTTReady
import net.minecraft.world.entity.player.Player
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.world.ContainerHelper

open class TransparentNodeElementInventory : WorldlyContainer, INBTTReady {
    @JvmField
    protected var transparentNodeRender: TransparentNodeElementRender? = null
    @JvmField
    protected var transparentNodeElement: TransparentNodeElement? = null
    var stackLimit: Int

    private var inv: NonNullList<ItemStack>

    constructor(size: Int, stackLimit: Int, TransparentnodeRender: TransparentNodeElementRender?) {
        inv = NonNullList.withSize(size, ItemStack.EMPTY)
        this.stackLimit = stackLimit
        transparentNodeRender = TransparentnodeRender
    }

    constructor(size: Int, stackLimit: Int, TransparentNodeElement: TransparentNodeElement?) {
        inv = NonNullList.withSize(size, ItemStack.EMPTY)
        this.stackLimit = stackLimit
        transparentNodeElement = TransparentNodeElement
    }

    override fun getContainerSize(): Int {
        return inv.size
    }

    override fun isEmpty(): Boolean {
        for (stack in inv) {
            if (!stack.isEmpty) return false
        }
        return true
    }

    override fun getItem(slot: Int): ItemStack {
        return if (slot >= 0 && slot < inv.size) inv[slot] else ItemStack.EMPTY
    }

    override fun removeItem(slot: Int, amt: Int): ItemStack {
        val stack = ContainerHelper.removeItem(inv, slot, amt)
        if (!stack.isEmpty) {
            setChanged()
        }
        return stack
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return ContainerHelper.takeItem(inv, slot)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        inv[slot] = stack
        if (!stack.isEmpty && stack.count > maxStackSize) {
            stack.count = maxStackSize
        }
        setChanged()
    }

    override fun getMaxStackSize(): Int {
        return stackLimit
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun startOpen(player: Player) {}
    override fun stopOpen(player: Player) {}
    
    override fun setChanged() {
        if (transparentNodeElement != null && !transparentNodeElement!!.node!!.isDestructing) {
            transparentNodeElement!!.inventoryChange(this)
            // Mark the BlockEntity as dirty so the chunk saves the data
            transparentNodeElement!!.markBlockEntityDirty()
        }
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        val tag = nbt.getCompound(str)
        ContainerHelper.loadAllItems(tag, inv)
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        val tag = CompoundTag()
        ContainerHelper.saveAllItems(tag, inv)
        nbt.put(str, tag)
    }

    override fun canPlaceItem(slot: Int, itemstack: ItemStack): Boolean {
        return true
    }

    override fun clearContent() {
        inv.clear()
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        return intArrayOf()
    }

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, side: Direction?): Boolean {
        return false
    }

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, side: Direction): Boolean {
        return false
    }
}

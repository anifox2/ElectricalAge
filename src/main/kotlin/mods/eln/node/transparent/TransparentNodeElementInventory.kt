package mods.eln.node.transparent

import mods.eln.misc.INBTTReady
import mods.eln.misc.Utils.readFromNBT
import mods.eln.misc.Utils.writeToNBT
import net.minecraft.world.entity.player.Player
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.Direction

open class TransparentNodeElementInventory : WorldlyContainer, INBTTReady {
    @JvmField
    protected var transparentNodeRender: TransparentNodeElementRender? = null
    @JvmField
    protected var transparentNodeElement: TransparentNodeElement? = null
    var stackLimit: Int

    constructor(size: Int, stackLimit: Int, TransparentnodeRender: TransparentNodeElementRender?) {
        inv = Array(size) { ItemStack.EMPTY }
        this.stackLimit = stackLimit
        transparentNodeRender = TransparentnodeRender
    }

    constructor(size: Int, stackLimit: Int, TransparentNodeElement: TransparentNodeElement?) {
        inv = Array(size) { ItemStack.EMPTY }
        this.stackLimit = stackLimit
        transparentNodeElement = TransparentNodeElement
    }

    private var inv: Array<ItemStack>

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
        var stack = getItem(slot)
        if (!stack.isEmpty) {
            if (stack.count <= amt) {
                setItem(slot, ItemStack.EMPTY)
            } else {
                stack = stack.split(amt)
                if (stack.count == 0) {
                    setItem(slot, ItemStack.EMPTY)
                }
            }
        }
        return stack
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        val stack = getItem(slot)
        if (!stack.isEmpty) {
            setItem(slot, ItemStack.EMPTY)
        }
        return stack
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        inv[slot] = stack
        if (!stack.isEmpty && stack.count > maxStackSize) {
            stack.count = maxStackSize
        }
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
        }
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        readFromNBT(nbt, str, this)
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        writeToNBT(nbt, str, this)
    }

    override fun canPlaceItem(slot: Int, itemstack: ItemStack): Boolean {
        for (idx in Direction.values()) {
            val lol = getSlotsForFace(idx)
            for (hohoho in lol) {
                if (hohoho == slot && canPlaceItemThroughFace(slot, itemstack, idx)) {
                    return true
                }
            }
        }
        return false
    }

    override fun clearContent() {
        for (i in inv.indices) {
            inv[i] = ItemStack.EMPTY
        }
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

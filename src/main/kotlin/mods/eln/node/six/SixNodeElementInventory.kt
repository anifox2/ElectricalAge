package mods.eln.node.six

import mods.eln.misc.INBTTReady
import mods.eln.misc.Utils.readFromNBT
import mods.eln.misc.Utils.writeToNBT
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

class SixNodeElementInventory : Container, INBTTReady {
    var sixnodeRender: SixNodeElementRender? = null
    var sixNodeElement: SixNodeElement? = null
    var stackLimit: Int

    constructor(size: Int, stackLimit: Int, sixnodeRender: SixNodeElementRender?) {
        inv = arrayOfNulls(size)
        this.stackLimit = stackLimit
        this.sixnodeRender = sixnodeRender
    }

    constructor(size: Int, stackLimit: Int, sixNodeElement: SixNodeElement?) {
        inv = arrayOfNulls(size)
        this.stackLimit = stackLimit
        this.sixNodeElement = sixNodeElement
    }

    private var inv: Array<ItemStack?>
    override fun getContainerSize(): Int {
        return inv.size
    }

    override fun getItem(slot: Int): ItemStack? {
        return if (slot >= inv.size) null else inv[slot]
    }

    override fun decrStackSize(slot: Int, amt: Int): ItemStack? {
        var stack = getItem(slot)
        if (stack != null) {
            if (stack.count <= amt) {
                setItem(slot, null)
            } else {
                stack = stack.splitStack(amt)
                if (stack.count == 0) {
                    setItem(slot, null)
                }
            }
        }
        return stack
    }

    override fun getItemOnClosing(slot: Int): ItemStack? {
        val stack = getItem(slot)
        if (stack != null) {
            setItem(slot, null)
        }
        return stack
    }

    override fun setItem(slot: Int, stack: ItemStack?) {
        try {
            inv[slot] = stack
            if (stack != null && stack.count > inventoryStackLimit) {
                stack.count = inventoryStackLimit
            }
        } catch (e: Exception) {
            // TODO: handle exception
        }
    }

    override fun getInventoryName(): String {
        return "tco.SixNodeInventory"
    }

    override fun getMaxStackSize(): Int {
        return stackLimit
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun startOpen() {}
    override fun stopOpen() {}
    override fun setChanged() {
        if (sixNodeElement != null && !sixNodeElement!!.sixNode!!.isDestructing) {
            sixNodeElement!!.inventoryChanged()
        }
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        readFromNBT(nbt, str, this)
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        writeToNBT(nbt, str, this)
    }

    override fun canPlaceItem(i: Int, itemstack: ItemStack): Boolean {
        return false
    }

    override fun hasCustomInventoryName(): Boolean {
        return false
    }
}

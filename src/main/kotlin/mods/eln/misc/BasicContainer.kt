package mods.eln.misc

import mods.eln.gui.ISlotSkin.SlotSkin
import mods.eln.gui.SlotWithSkin
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import kotlin.math.min

import net.minecraft.world.inventory.MenuType

open class BasicContainer @JvmOverloads constructor(player: Player, var inventory: Container, slot: Array<Slot>, menuType: MenuType<*>? = null, windowId: Int = 0) : AbstractContainerMenu(menuType, windowId) {
    init {
        for (i in slot.indices) {
            addSlot(slot[i])
        }
        bindPlayerInventory(player.inventory)
    }

    override fun stillValid(player: Player): Boolean {
        return inventory.stillValid(player)
    }

    private fun bindPlayerInventory(inventoryPlayer: Inventory) {
        val baseX = 8
        val baseY = 84
        for (i in 0..2) {
            for (j in 0..8) {
                addSlot(SlotWithSkin(inventoryPlayer, j + i * 9 + 9, baseX + j * 18, baseY + i * 18, SlotSkin.medium))
            }
        }
        for (i in 0..8) {
            addSlot(SlotWithSkin(inventoryPlayer, i, baseX + i * 18, baseY + 58, SlotSkin.medium))
        }
    }

    override fun addSlot(slot: Slot): Slot {
        return super.addSlot(slot)
    }

    override fun quickMoveStack(player: Player, slotId: Int): ItemStack {
        val slot = slots[slotId]
        if (slot != null && slot.hasItem()) {
            val itemstack1 = slot.item
            val invSize = inventory.containerSize
            if (slotId < invSize) {
                moveItemStackTo(itemstack1, invSize, slots.size, true)
            } else {
                if (!moveItemStackTo(itemstack1, 0, invSize, true)) {
                    if (slotId < invSize + 27) {
                        moveItemStackTo(itemstack1, invSize + 27, slots.size, false)
                    } else {
                        moveItemStackTo(itemstack1, invSize, invSize + 27, false)
                    }
                }
            }

            if (itemstack1.count == 0) {
                slot.set(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }
            return itemstack1
        }

        return ItemStack.EMPTY
    }

    override fun moveItemStackTo(par1ItemStack: ItemStack, par2: Int, par3: Int, par4: Boolean): Boolean {
        var flag1 = false
        var k = par2
        if (par4) {
            k = par3 - 1
        }
        var slot: Slot
        var itemstack1: ItemStack
        if (par1ItemStack.isStackable) {
            while (par1ItemStack.count > 0 && (!par4 && k < par3 || par4 && k >= par2)) {
                slot = slots[k]
                itemstack1 = slot.item
                if (slot.mayPlace(par1ItemStack) && !itemstack1.isEmpty && ItemStack.isSameItemSameTags(par1ItemStack, itemstack1)) {
                    val l = itemstack1.count + par1ItemStack.count
                    val maxSize = min(slot.maxStackSize.toDouble(), par1ItemStack.maxStackSize.toDouble()).toInt()
                    if (l <= maxSize) {
                        par1ItemStack.count = 0
                        itemstack1.count = l
                        slot.setChanged()
                        flag1 = true
                    } else if (itemstack1.count < maxSize) {
                        par1ItemStack.count -= maxSize - itemstack1.count
                        itemstack1.count = maxSize
                        slot.setChanged()
                        flag1 = true
                    }
                }
                if (par4) {
                    --k
                } else {
                    ++k
                }
            }
        }
        if (par1ItemStack.count > 0) {
            k = if (par4) {
                par3 - 1
            } else {
                par2
            }
            while (!par4 && k < par3 || par4 && k >= par2) {
                slot = slots[k]
                itemstack1 = slot.item
                if (itemstack1.isEmpty && slot.mayPlace(par1ItemStack)) {
                    val l = par1ItemStack.count
                    val maxSize = min(slot.maxStackSize.toDouble(), par1ItemStack.maxStackSize.toDouble()).toInt()
                    if (l <= maxSize) {
                        slot.set(par1ItemStack.copy())
                        slot.setChanged()
                        par1ItemStack.count = 0
                        flag1 = true
                        break
                    } else {
                        par1ItemStack.count -= maxSize
                        val newItemStack = par1ItemStack.copy()
                        newItemStack.count = maxSize
                        slot.set(newItemStack)
                        slot.setChanged()
                        flag1 = true
                        break
                    }
                }
                if (par4) {
                    --k
                } else {
                    ++k
                }
            }
        }
        return flag1
    }
}

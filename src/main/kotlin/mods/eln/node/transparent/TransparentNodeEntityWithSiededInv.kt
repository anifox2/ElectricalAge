package mods.eln.node.transparent

// Seems unused.

/*
class TransparentNodeBlockEntityWithSiededInv : TransparentNodeBlockEntity(), ISidedInventory {
    override fun getSidedInventory(): ISidedInventory {
        if (level.isRemote) {
            if (elementRender == null) return instance
            val i = elementRender!!.inventory
            if (i != null && i is ISidedInventory) {
                return i
            }
        } else {
            val node = node
            if (node != null && node is TransparentNode) {
                val i = node.getInventory(null)
                if (i != null && i is ISidedInventory) {
                    return i
                }
            }
        }
        return instance
    }

    override fun getContainerSize(): Int {
        return sidedInventory.sizeInventory
    }

    override fun getItem(var1: Int): ItemStack {
        return sidedInventory.getItem(var1)
    }

    override fun decrStackSize(var1: Int, var2: Int): ItemStack {
        return sidedInventory.decrStackSize(var1, var2)
    }

    override fun getItemOnClosing(var1: Int): ItemStack {
        return sidedInventory.getItemOnClosing(var1)
    }

    override fun setItem(var1: Int, var2: ItemStack) {
        sidedInventory.setItem(var1, var2)
    }

    override fun getInventoryName(): String {
        return sidedInventory.inventoryName
    }

    override fun hasCustomInventoryName(): Boolean {
        return sidedInventory.hasCustomInventoryName()
    }

    override fun getMaxStackSize(): Int {
        return sidedInventory.inventoryStackLimit
    }

    override fun stillValid(var1: Player): Boolean {
        return sidedInventory.stillValid(var1)
    }

    override fun startOpen() {
        sidedInventory.startOpen()
    }

    override fun stopOpen() {
        sidedInventory.stopOpen()
    }

    override fun canPlaceItem(var1: Int, var2: ItemStack): Boolean {
        return sidedInventory.canPlaceItem(var1, var2)
    }

    override fun getAccessibleSlotsFromSide(var1: Int): IntArray {
        return sidedInventory.getAccessibleSlotsFromSide(var1)
    }

    override fun canInsertItem(var1: Int, var2: ItemStack, var3: Int): Boolean {
        return sidedInventory.canInsertItem(var1, var2, var3)
    }

    override fun canExtractItem(var1: Int, var2: ItemStack, var3: Int): Boolean {
        return sidedInventory.canExtractItem(var1, var2, var3)
    }
}
 */

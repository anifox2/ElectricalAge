package mods.eln.generic

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

open class GenericItemBlockUsingDamageDescriptor(name: String, iconName: String? = null, registerItem: Boolean = true) : GenericItemUsingDamageDescriptor(name, iconName, registerItem) {
    var parentItem: Item? = null
    var damage: Int = 0

    open fun setParent(item: Item, damage: Int) {
        this.parentItem = item
        this.damage = damage
    }

    override fun newItemStack(amount: Int): ItemStack {
        if (parentItem != null) {
            val stack = ItemStack(parentItem!!, amount)
            stack.damageValue = damage
            return stack
        }
        return super.newItemStack(amount)
    }

    override fun getDefaultNBT(): CompoundTag {
        return CompoundTag()
    }

    companion object {
        @JvmStatic
        fun getDescriptor(stack: net.minecraft.world.item.ItemStack): GenericItemBlockUsingDamageDescriptor? {
            return GenericItemUsingDamageDescriptor.getDescriptor(stack) as? GenericItemBlockUsingDamageDescriptor
        }
    }
}

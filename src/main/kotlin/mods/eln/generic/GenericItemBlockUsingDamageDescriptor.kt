package mods.eln.generic

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import mods.eln.misc.elnMetadata

// Blocks with damage-based variants should be driven by their shared BlockItem;
// avoid auto-registering standalone items by default.
open class GenericItemBlockUsingDamageDescriptor(name: String, iconName: String? = null, registerItem: Boolean = false) : GenericItemUsingDamageDescriptor(name, iconName, registerItem) {

    open fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
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

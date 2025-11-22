package mods.eln.generic

import net.minecraft.world.item.Item
import net.minecraft.nbt.CompoundTag

open class GenericItemBlockUsingDamageDescriptor(name: String, iconName: String? = null) : GenericItemUsingDamageDescriptor(name, iconName) {
    var parentItem: Item? = null
    var damage: Int = 0

    open fun setParent(item: Item, damage: Int) {
        this.parentItem = item
        this.damage = damage
    }

    open fun getDefaultNBT(): CompoundTag {
        return CompoundTag()
    }
}

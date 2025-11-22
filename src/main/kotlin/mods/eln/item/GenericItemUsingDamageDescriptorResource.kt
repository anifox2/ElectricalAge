package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item

open class GenericItemUsingDamageDescriptorResource(name: String) : GenericItemUsingDamageDescriptor(name) {
    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addResource(newItemStack())
    }
}

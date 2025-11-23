package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item

open class GenericItemUsingDamageDescriptorUpgrade : GenericItemUsingDamageDescriptor {
    constructor(name: String?) : super(name!!) {}
    constructor(name: String?, iconName: String?) : super(name!!, iconName) {}

    override fun setParent(registry: Any?, id: Int) {
        super.setParent(registry, id)
        //Data.addUpgrade(newItemStack())
    }
}

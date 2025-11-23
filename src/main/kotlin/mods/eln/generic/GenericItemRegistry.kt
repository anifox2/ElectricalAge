package mods.eln.generic

import java.util.Hashtable

class GenericItemRegistry {
    val subItemList = Hashtable<Int, GenericItemUsingDamageDescriptor>()
    
    fun addElement(id: Int, descriptor: GenericItemUsingDamageDescriptor) {
        subItemList[id] = descriptor
        descriptor.setParent(this, id)
    }
    
    fun addWithoutRegistry(id: Int, descriptor: GenericItemUsingDamageDescriptor) {
        subItemList[id] = descriptor
        descriptor.setParent(this, id)
    }
    
    fun addDescriptor(id: Int, descriptor: GenericItemUsingDamageDescriptor) {
        addElement(id, descriptor)
    }

    fun getDescriptor(itemStack: net.minecraft.world.item.ItemStack?): GenericItemUsingDamageDescriptor? {
        if (itemStack == null) return null
        return subItemList[itemStack.damageValue]
    }

    fun getDescriptor(id: Int): GenericItemUsingDamageDescriptor? {
        return subItemList[id]
    }
}

package mods.eln.generic

import java.util.Hashtable

class GenericItemRegistry {
    val subItemList = Hashtable<Int, GenericItemUsingDamageDescriptor>()
    
    fun addElement(id: Int, descriptor: GenericItemUsingDamageDescriptor) {
        subItemList[id] = descriptor
    }
    
    fun addWithoutRegistry(id: Int, descriptor: GenericItemUsingDamageDescriptor) {
        subItemList[id] = descriptor
    }
}

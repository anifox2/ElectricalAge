package mods.eln.generic

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.Hashtable
import java.util.ArrayList

import mods.eln.misc.elnMetadata

open class GenericItemUsingDamage<Descriptor : GenericItemUsingDamageDescriptor> : Item(Properties()) {

    val subItemList = Hashtable<Int, Descriptor>()
    val orderList = ArrayList<Int>()
    val descriptors = ArrayList<Descriptor>()

    var defaultElement: Descriptor? = null

    fun doubleEntry(src: Int, dst: Int) {
        subItemList[dst] = subItemList[src]!!
    }

    fun addDescriptor(damage: Int, descriptor: Descriptor) {
        subItemList[damage] = descriptor
        orderList.add(damage)
        descriptors.add(descriptor)
        descriptor.setParent(this, damage)
    }

    fun addElement(damage: Int, descriptor: Descriptor) {
        addDescriptor(damage, descriptor)
    }

    fun addWithoutRegistry(damage: Int, descriptor: Descriptor) {
        addDescriptor(damage, descriptor)
    }

    fun getDescriptor(damage: Int): Descriptor? {
        return subItemList[damage]
    }

    fun getDescriptor(itemStack: ItemStack?): Descriptor? {
        if (itemStack == null) return defaultElement
        if (itemStack.item != this) return defaultElement
        return getDescriptor(itemStack.elnMetadata)
    }
    
    override fun getDescriptionId(stack: ItemStack): String {
        val desc = getDescriptor(stack)
        return desc?.name ?: super.getDescriptionId(stack)
    }
}

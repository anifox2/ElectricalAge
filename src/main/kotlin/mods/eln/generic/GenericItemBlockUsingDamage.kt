package mods.eln.generic

import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.ItemStack
import java.util.Hashtable
import java.util.ArrayList

open class GenericItemBlockUsingDamage<Descriptor : GenericItemBlockUsingDamageDescriptor>(block: Block) : BlockItem(block, Properties()) {

    val subItemList = Hashtable<Int, Descriptor>()
    val orderList = ArrayList<Int>()
    val descriptors = ArrayList<Descriptor>()

    var defaultElement: Descriptor? = null

    fun setDefaultElement(descriptor: Descriptor) {
        defaultElement = descriptor
    }

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
        return getDescriptor(itemStack.damageValue)
    }
    
    override fun getDescriptionId(stack: ItemStack): String {
        val desc = getDescriptor(stack)
        return desc?.name ?: super.getDescriptionId(stack)
    }
}

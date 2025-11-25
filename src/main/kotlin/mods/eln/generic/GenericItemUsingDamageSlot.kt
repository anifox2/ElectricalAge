package mods.eln.generic

import mods.eln.gui.ISlotSkin
import mods.eln.gui.SlotWithSkin
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

open class GenericItemUsingDamageSlot(
    inventory: Container,
    index: Int,
    x: Int,
    y: Int,
    val stackLimit: Int,
    val allowedClasses: Array<Class<*>>,
    skin: ISlotSkin.SlotSkin = ISlotSkin.SlotSkin.medium,
    val tooltip: Array<String>? = null
) : SlotWithSkin(inventory, index, x, y, skin) {
    override fun mayPlace(stack: ItemStack): Boolean {
        return allowedClasses.any { it.isAssignableFrom(stack.item.javaClass) } || 
               allowedClasses.any { it.isInstance(GenericItemUsingDamageDescriptor.getDescriptor(stack)) }
    }
}

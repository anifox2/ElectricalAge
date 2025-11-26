package mods.eln.item

import mods.eln.Eln
import mods.eln.generic.GenericItemUsingDamageSlot
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.gui.ISlotSkin
import mods.eln.item.regulator.IRegulatorDescriptor
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

class RegulatorSlot(
    inventory: Container,
    index: Int,
    x: Int,
    y: Int,
    stackLimit: Int,
    val type: Array<IRegulatorDescriptor.RegulatorType>,
    skin: ISlotSkin.SlotSkin,
    comment: String = "Regulator slot"
) : GenericItemUsingDamageSlot(
    inventory,
    index,
    x,
    y,
    stackLimit,
    arrayOf(IRegulatorDescriptor::class.java),
    skin,
    arrayOf(comment)
) {
    override fun mayPlace(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        
        // Try multiple lookup methods to find the descriptor
        val regDescriptor: IRegulatorDescriptor? = 
            // Method 1: Try legacy sharedItem registry (most common for regulators)
            (Eln.sharedItem.getDescriptor(stack) as? IRegulatorDescriptor)
            // Method 2: Try new registration system
            ?: (GenericItemUsingDamageDescriptor.getDescriptor(stack) as? IRegulatorDescriptor)
            // Method 3: Try by name (fallback)
            ?: run {
                val name = stack.item.descriptionId
                GenericItemUsingDamageDescriptor.getByName(
                    name.removePrefix("item.eln.").replace("_", " ")
                ) as? IRegulatorDescriptor
            }
        
        if (regDescriptor == null) return false
        
        return type.any { it == regDescriptor.getType() }
    }
}

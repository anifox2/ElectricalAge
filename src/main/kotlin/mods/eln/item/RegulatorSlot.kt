package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageSlot
import mods.eln.gui.ISlotSkin
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

import mods.eln.generic.GenericItemUsingDamageDescriptor

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
        if (!super.mayPlace(stack)) return false
        val descriptor = GenericItemUsingDamageDescriptor.getDescriptor(stack) as? IRegulatorDescriptor ?: return false
        return type.any { it == descriptor.type }
    }
}

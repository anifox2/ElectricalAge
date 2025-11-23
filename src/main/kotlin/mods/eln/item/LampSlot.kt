package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageSlot
import mods.eln.misc.Utils
import mods.eln.sixnode.lampsocket.LampSocketType
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import mods.eln.i18n.I18N.tr
import mods.eln.gui.ISlotSkin.SlotSkin

class LampSlot(
    inventory: Container,
    slot: Int,
    x: Int,
    y: Int,
    stackLimit: Int,
    val socket: LampSocketType
) : GenericItemUsingDamageSlot(
    inventory,
    slot,
    x,
    y,
    stackLimit,
    arrayOf(LampDescriptor::class.java),
    SlotSkin.medium,
    arrayOf(tr("Lamp slot"))
) {
    override fun mayPlace(itemStack: ItemStack): Boolean {
        if (!super.mayPlace(itemStack)) return false
        val descriptor = Utils.getItemObject(itemStack) as? LampDescriptor
        return descriptor?.socket == socket
    }
}

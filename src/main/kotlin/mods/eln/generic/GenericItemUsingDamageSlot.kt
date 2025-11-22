package mods.eln.generic

import mods.eln.gui.ISlotSkin
import mods.eln.gui.SlotWithSkin
import net.minecraft.world.Container

class GenericItemUsingDamageSlot(
    inventory: Container,
    index: Int,
    x: Int,
    y: Int,
    val stackLimit: Int,
    val allowedClasses: Array<Class<*>>,
    skin: ISlotSkin.SlotSkin = ISlotSkin.SlotSkin.Medium,
    val tooltip: Array<String>? = null
) : SlotWithSkin(inventory, index, x, y, skin) {
    
}

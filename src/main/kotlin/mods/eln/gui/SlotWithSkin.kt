package mods.eln.gui

import mods.eln.gui.ISlotSkin.SlotSkin
import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot

open class SlotWithSkin(
    inventory: Container?,
    slotIndex: Int,
    xDisplayPosition: Int,
    yDisplayPosition: Int,
    var skin: SlotSkin
): Slot(
    inventory,
    slotIndex,
    xDisplayPosition,
    yDisplayPosition
), ISlotSkin {
    override fun getSlotSkin(): SlotSkin {
        return skin
    }
}

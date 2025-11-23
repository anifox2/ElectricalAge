package mods.eln.misc

import mods.eln.Eln
import net.minecraft.world.entity.player.Player

fun Player?.isHoldingMeter(): Boolean {
    if (this == null) return false
    val equippedItem = mainHandItem
    return (Eln.multiMeterElement?.checkSameItemStack(equippedItem) == true
        || Eln.thermometerElement?.checkSameItemStack(equippedItem) == true
        || Eln.allMeterElement?.checkSameItemStack(equippedItem) == true)
}

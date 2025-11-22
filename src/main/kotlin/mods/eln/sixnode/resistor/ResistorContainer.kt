package mods.eln.sixnode.resistor

import mods.eln.misc.BasicContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot

class ResistorContainer(player: Player, inventory: Container) : BasicContainer(player, inventory, arrayOf()) {
    companion object {
        const val coreId = 0
    }
}

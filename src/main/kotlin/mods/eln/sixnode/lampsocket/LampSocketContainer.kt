package mods.eln.sixnode.lampsocket

import mods.eln.gui.ISlotSkin
import mods.eln.item.LampSlot
import mods.eln.misc.BasicContainer
import mods.eln.node.six.SixNodeItemSlot
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import mods.eln.i18n.I18N.tr

class LampSocketContainer(player: Player, inventory: Container, descriptor: LampSocketDescriptor) : BasicContainer(player, inventory, arrayOf(
    LampSlot(inventory, lampSlotId, 70 + 0, 57, 1, descriptor.socketType),
    SixNodeItemSlot(inventory, cableSlotId, 70 + 18, 57, 1, arrayOf(ElectricalCableDescriptor::class.java),
        ISlotSkin.SlotSkin.medium, arrayOf(tr("Electrical cable slot")))
)) {
    companion object {
        const val lampSlotId = 0
        const val cableSlotId = 1
    }
}

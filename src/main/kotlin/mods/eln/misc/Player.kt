package mods.eln.misc

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.world.entity.player.ServerPlayer

/**
 * Contains utilities for dealing with player entities.
 */

fun Player.totalItemsCarried(stack: ItemStack): Int {
    return inventory.mainInventory
        .filterNotNull()
        .filter { it.isItemEqual(stack) }
        .sumOf { it.count }
}

fun Player.removeMultipleItems(stack: ItemStack, count: Int) {
    if(Utils.isCreative(this as ServerPlayer)) return
    assert(count <= totalItemsCarried(stack))
    var left = count
    try {
        inventory.mainInventory.indices.reversed().forEach { i ->
            val invStack = inventory.mainInventory[i]
            if (invStack?.isItemEqual(stack) == true) {
                left -= invStack.splitStack(invStack.count.coerceAtMost(left)).count
                assert(invStack.count >= 0)
                // Black magic used to synchronize immediately with the client.
                val slot = openContainer.getSlotFromInventory(inventory, i)
                playerNetServerHandler.sendPacket(S2FPacketSetSlot(openContainer.windowId, slot.slotNumber, invStack))
                if (left == 0) return
            }
        }
    } finally {
        inventory.setChanged()
    }
}

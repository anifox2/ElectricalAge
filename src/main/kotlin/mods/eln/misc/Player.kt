package mods.eln.misc

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.server.level.ServerPlayer

/**
 * Contains utilities for dealing with player entities.
 */

fun Player.totalItemsCarried(stack: ItemStack): Int {
    return inventory.items
        .filter { !it.isEmpty }
        .filter { it.item == stack.item }
        .sumOf { it.count }
}

fun Player.removeMultipleItems(stack: ItemStack, count: Int) {
    if (this.isCreative) return
    // assert(count <= totalItemsCarried(stack))
    var left = count
    
    for (i in inventory.items.indices.reversed()) {
        val invStack = inventory.items[i]
        if (!invStack.isEmpty && invStack.item == stack.item) {
            val toRemove = invStack.count.coerceAtMost(left)
            invStack.shrink(toRemove)
            left -= toRemove
            
            if (this is ServerPlayer) {
                val slot = containerMenu.slots.firstOrNull { it.container == inventory && it.containerSlot == i }
                if (slot != null) {
                    connection.send(ClientboundContainerSetSlotPacket(containerMenu.containerId, containerMenu.stateId, slot.index, invStack))
                }
            }
            
            if (left == 0) break
        }
    }
    inventory.setChanged()
}

package mods.eln.item

import mods.eln.misc.Utils
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer

abstract class ItemMovingHelper {
    abstract fun acceptsStack(stack: ItemStack): Boolean
    abstract fun newStackOfSize(items: Int): ItemStack

    fun move(src: Inventory, dst: Container, dstSlot: Int, desired: Int) {
        if (src.player is ServerPlayer && (src.player as ServerPlayer).isCreative) {
            if (desired == 0) {
                dst.setItem(dstSlot, ItemStack.EMPTY)
            } else {
                dst.setItem(dstSlot, newStackOfSize(desired))
            }
            return
        }
        var now = 0
        val stack = dst.getItem(dstSlot)
        if (!stack.isEmpty) {
            now = stack.count
        }
        // Utils.println(String.format("IMH.m: now %d, desired %d", now, desired))
        if (now < desired) {
            var diff = desired - now
            for (idx in 0 until src.containerSize) {
                var invStack = src.getItem(idx)
                if (invStack.isEmpty) continue
                if (!acceptsStack(invStack)) continue
                val move = Math.min(invStack.count, diff)
                diff -= move
                invStack.shrink(move)
                if (invStack.isEmpty) {
                    invStack = ItemStack.EMPTY
                }
                src.setItem(idx, invStack)
                // Grissess: We need to send this immediately to sync with the client
                // syncItemInSlot(src, idx); // TODO: Implement sync if needed
                if (diff <= 0) break
            }
            val moved = (desired - now) - diff
            // Utils.println(String.format("IMH.m: moved %d into node", moved))
            if (moved > 0) {
                val newStack = newStackOfSize(now + moved)
                dst.setItem(dstSlot, newStack)
            }
        } else if (now > desired) {
            var diff = now - desired
            // Utils.println(String.format("IMH.m: moving %d out of node", diff))
            val newStack = newStackOfSize(desired)
            if (newStack.isEmpty) {
                dst.setItem(dstSlot, ItemStack.EMPTY)
            } else {
                dst.setItem(dstSlot, newStack)
            }
            // Try to put back into inventory
            // This part was missing in the snippet but implied by logic
            // For now, just drop or add to inventory
            if (diff > 0) {
                 val refund = newStackOfSize(diff)
                 if (!src.add(refund)) {
                     src.player.drop(refund, false)
                 }
            }
        }
    }
}

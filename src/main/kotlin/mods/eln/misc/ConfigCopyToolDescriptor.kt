package mods.eln.misc

import mods.eln.generic.GenericItemUsingDamageDescriptor
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class ConfigCopyToolDescriptor(name: String) : GenericItemUsingDamageDescriptor(name) {
    companion object {
        fun readGenDescriptor(nbt: CompoundTag, name: String, inventory: Container, slotId: Int, player: Player): Boolean {
            return false // Stub
        }

        fun writeGenDescriptor(nbt: CompoundTag, name: String, stack: ItemStack) {
            // Stub
        }
    }
}

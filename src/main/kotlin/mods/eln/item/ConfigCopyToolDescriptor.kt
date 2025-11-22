package mods.eln.item

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

import mods.eln.generic.GenericItemUsingDamageDescriptor

class ConfigCopyToolDescriptor(name: String) : GenericItemUsingDamageDescriptor(name) {
    companion object {
        fun readGenDescriptor(compound: CompoundTag, key: String, inventory: Container, slotId: Int, player: Player): Boolean {
            return false
        }

        fun writeGenDescriptor(compound: CompoundTag, key: String, stack: ItemStack?) {
        }

        fun readCableType(compound: CompoundTag, key: String, inventory: Container, slotId: Int, player: Player): Boolean {
            return false
        }

        fun writeCableType(compound: CompoundTag, key: String, stack: ItemStack?) {
        }
    }
}

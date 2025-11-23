package mods.eln.item

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.node.six.SixNodeElementInventory

class ConfigCopyToolDescriptor(name: String) : GenericItemUsingDamageDescriptor(name) {
    companion object {
        @JvmStatic
        fun readGenDescriptor(compound: CompoundTag, key: String, inventory: Container, slotId: Int, player: Player): Boolean {
            return false
        }

        @JvmStatic
        fun writeGenDescriptor(compound: CompoundTag, key: String, stack: ItemStack?) {
        }

        @JvmStatic
        fun readCableType(compound: CompoundTag, key: String, inventory: Container, slotId: Int, player: Player): Boolean {
            return false
        }

        @JvmStatic
        fun readCableType(compound: CompoundTag, inventory: Container, slotId: Int, player: Player): Boolean {
            return readCableType(compound, "cable", inventory, slotId, player)
        }

        @JvmStatic
        fun writeCableType(compound: CompoundTag, key: String, stack: ItemStack?) {
        }

        @JvmStatic
        fun writeCableType(compound: CompoundTag, stack: ItemStack?) {
            writeCableType(compound, "cable", stack)
        }

        @JvmStatic
        fun readVanillaStack(compound: CompoundTag, key: String, inventory: Container, slotId: Int, player: Player): Boolean {
            return false
        }

        @JvmStatic
        fun writeVanillaStack(compound: CompoundTag, key: String, stack: ItemStack?) {
        }
    }
}

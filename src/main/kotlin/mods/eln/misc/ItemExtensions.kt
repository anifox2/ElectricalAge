package mods.eln.misc

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

val ItemStack.nbt: CompoundTag
    get() = this.getOrCreateTag()

fun ItemStack.getDouble(key: String): Double {
    return this.nbt.getDouble(key)
}

fun ItemStack.putDouble(key: String, value: Double) {
    this.nbt.putDouble(key, value)
}

fun ItemStack.getString(key: String): String {
    return this.nbt.getString(key)
}

fun ItemStack.putString(key: String, value: String) {
    this.nbt.putString(key, value)
}

fun ItemStack.getInt(key: String): Int {
    return this.nbt.getInt(key)
}

fun ItemStack.setInt(key: String, value: Int) {
    this.nbt.putInt(key, value)
}

fun ItemStack.getBoolean(key: String): Boolean {
    return this.nbt.getBoolean(key)
}

fun ItemStack.putBoolean(key: String, value: Boolean) {
    this.nbt.putBoolean(key, value)
}

fun ItemStack.getByte(key: String): Byte {
    return this.nbt.getByte(key)
}

fun ItemStack.putByte(key: String, value: Byte) {
    this.nbt.putByte(key, value)
}

fun ItemStack.getShort(key: String): Short {
    return this.nbt.getShort(key)
}

fun ItemStack.setShort(key: String, value: Short) {
    this.nbt.putShort(key, value)
}

var ItemStack.elnMetadata: Int
    get() = if (this.hasTag() && this.tag!!.contains("eln_metadata")) this.tag!!.getInt("eln_metadata") else this.damageValue
    set(value) {
        this.getOrCreateTag().putInt("eln_metadata", value)
        // For compatibility with code that might check damageValue, we could set it too, 
        // but only if it doesn't interfere with durability.
        // For now, we rely on NBT.
        this.damageValue = value // Try setting damage too, just in case some renderers use it
    }

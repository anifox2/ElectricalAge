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

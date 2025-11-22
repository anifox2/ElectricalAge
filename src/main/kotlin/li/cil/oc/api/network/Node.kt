package li.cil.oc.api.network

import net.minecraft.nbt.CompoundTag

interface Node {
    fun network(): Any?
    fun address(): String?
    fun load(nbt: CompoundTag)
    fun save(nbt: CompoundTag)
    fun remove()
}

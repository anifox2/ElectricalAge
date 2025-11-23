package mods.eln.ghost

import mods.eln.Eln
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData

class GhostManagerNbt : SavedData() {
    override fun isDirty(): Boolean {
        return true
    }

    fun load(nbt: CompoundTag) {
        Eln.ghostManager!!.loadFromNBT(nbt)
    }

    override fun save(nbt: CompoundTag): CompoundTag {
        Eln.ghostManager!!.save(nbt)
        return nbt
    }
}

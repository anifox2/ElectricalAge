package mods.eln.node

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData

class NodeManagerNbt : SavedData() {
    // override fun isDirty(): Boolean {
    //    return true
    // }

    fun load(nbt: CompoundTag) {
        NodeManager.instance!!.loadFromNbt(nbt)
    }

    override fun save(nbt: CompoundTag): CompoundTag {
        //NodeManager.instance.saveToNbt(nbt, Integer.MIN_VALUE);
        return nbt
    }
}

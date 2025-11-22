package mods.eln.node

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.LevelSavedData

class NodeManagerNbt(par1Str: String?) : WorldSavedData(par1Str) {
    override fun isDirty(): Boolean {
        return true
    }

    override fun readFromNBT(nbt: CompoundTag) {
        NodeManager.instance!!.loadFromNbt(nbt)
    }

    override fun writeToNBT(nbt: CompoundTag) {
        //NodeManager.instance.saveToNbt(nbt, Integer.MIN_VALUE);
    }
}

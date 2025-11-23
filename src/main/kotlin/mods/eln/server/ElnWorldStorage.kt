package mods.eln.server

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.server.level.ServerLevel

class ElnWorldStorage : SavedData() {
    var dim = 0

    override fun save(nbt: CompoundTag): CompoundTag {
        nbt.putInt("dim", dim)
        ServerEventListener.writeToEaWorldNBT(nbt, dim)
        return nbt
    }

    companion object {
        const val key = "eln.worldStorage"

        fun load(nbt: CompoundTag): ElnWorldStorage {
            val data = ElnWorldStorage()
            data.dim = nbt.getInt("dim")
            ServerEventListener.readFromEaWorldNBT(nbt)
            return data
        }

        @JvmStatic
        fun forWorld(world: Level): ElnWorldStorage {
            if (world is ServerLevel) {
                val storage = world.dataStorage
                return storage.computeIfAbsent(::load, ::ElnWorldStorage, key)
            }
            return ElnWorldStorage()
        }
    }
}

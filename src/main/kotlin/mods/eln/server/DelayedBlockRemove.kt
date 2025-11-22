package mods.eln.server

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.server.DelayedTaskManager.ITask
import net.minecraft.world.level.block.Blocks
import java.util.*

class DelayedBlockRemove private constructor(var c: Coordinate) : ITask {
    override fun run() {
        BLOCKS.remove(c)
        val level = mods.eln.misc.Utils.getLevel(c.dimension)
        if (level != null) {
            level.setBlock(c.toBlockPos(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3)
        }
    }

    companion object {
        private val BLOCKS: MutableSet<Coordinate> = HashSet()
        @JvmStatic
        fun clear() {
            BLOCKS.clear()
        }

        @JvmStatic
        fun add(c: Coordinate) {
            if (BLOCKS.contains(c)) return
            BLOCKS.add(c)
            Eln.delayedTask.add(DelayedBlockRemove(c))
        }
    }
}

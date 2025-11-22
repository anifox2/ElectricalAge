package mods.eln.server

import mods.eln.Eln
import mods.eln.misc.Utils
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.level.ChunkEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.server.ServerLifecycleHooks
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import java.util.*

class OreRegenerate {
    var jobs = LinkedList<ChunkRef>()
    var alreadyLoadedChunks = HashSet<ChunkRef>()

    fun clear() {
        jobs.clear()
        alreadyLoadedChunks.clear()
    }

    @SubscribeEvent
    fun tick(event: ServerTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        for (idx in 0..0) {
            if (!jobs.isEmpty()) {
                val j = jobs.pollLast()
                if (!Eln.saveConfig.reGenOre && !Eln.instance.forceOreRegen) return
                
                val server = ServerLifecycleHooks.getCurrentServer()
                val level = server?.getLevel(Utils.getLevelKey(j.worldId)) ?: return
                val chunk = level.getChunk(j.x, j.z)
                
                var y = 0
                while (y < 60) {
                    var z = y and 1
                    while (z < 16) {
                        var x = y and 1
                        while (x < 16) {
                            // Using 0,0,0 relative to chunk? No, getBlockState expects global pos usually, but chunk.getBlockState might expect local?
                            // In 1.20.1 LevelChunk.getBlockState(BlockPos) expects global pos?
                            // Actually LevelChunk.getBlockState(x, y, z) exists? No.
                            // We should use level.getBlockState(pos) but that loads chunks.
                            // chunk.getBlockState(pos) is available.
                            // We need global pos.
                            val pos = BlockPos(j.x * 16 + x, y, j.z * 16 + z)
                            if (chunk.getBlockState(pos).block === Eln.instance.oreBlock) {
                                return
                            }
                            x += 2
                        }
                        z += 2
                    }
                    y += 2
                }
                Utils.println("Regenerated! " + jobs.size)
                // TODO: Fix ore generation logic
                /*
                for (d in Eln.instance.oreItem?.descriptors ?: emptyList()) {
                    d?.generate(level.random, j.x, j.z, level, null, null)
                }
                */
            }
        }
    }

    @SubscribeEvent
    fun chunkLoad(e: ChunkEvent.Load) {
        val level = e.level as? Level ?: return
        if (level.isClientSide || !Eln.saveConfig.reGenOre) return
        val c = e.chunk
        val ref = ChunkRef(c.pos.x, c.pos.z, Utils.getDimensionId(level))
        if (alreadyLoadedChunks.contains(ref)) {
            // Utils.println("Already regenerated!")
            return
        }
        alreadyLoadedChunks.add(ref)
        jobs.addFirst(ref)
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }
}

class ChunkRef(var x: Int, var z: Int, var worldId: Int) {
    override fun hashCode(): Int {
        return x * z + (worldId shl 20)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ChunkRef) return false
        return other.x == x && other.z == z && other.worldId == worldId
    }
}

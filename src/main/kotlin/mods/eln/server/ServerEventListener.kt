package mods.eln.server

import mods.eln.Eln
import mods.eln.item.electricalitem.TreeCapitation.process
import mods.eln.misc.Coordinate
import mods.eln.misc.Utils
import mods.eln.node.NodeManager
import mods.eln.server.ElnWorldStorage.Companion.forWorld
import net.minecraft.world.entity.LightningBolt
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelResource
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

class ServerEventListener {
    private var lightningListNext = LinkedList<LightningBolt>()
    private var lightningList = LinkedList<LightningBolt>()
    
    @SubscribeEvent
    fun tick(event: ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        lightningList = lightningListNext
        lightningListNext = LinkedList()
        process(0.05)
    }

    @SubscribeEvent
    fun onNewEntity(event: EntityConstructing) {
        if (event.entity is LightningBolt) {
            lightningListNext.add(event.entity as LightningBolt)
        }
    }

    fun clear() {
        lightningList.clear()
    }

    fun getLightningClosestTo(c: Coordinate): Double {
        var best = 10000000.0
        for (l in lightningList) {
            if (c.world() !== l.level()) continue
            val d = l.distanceToSqr(c.x.toDouble(), c.y.toDouble(), c.z.toDouble())
            val dist = Math.sqrt(d)
            if (dist < best) best = dist
        }
        return best
    }

    private val loadedWorlds = HashSet<Int>()
    
    @SubscribeEvent
    fun onWorldLoad(e: LevelEvent.Load) {
        val level = e.level as? Level ?: return
        if (level.isClientSide) return
        val dimId = Utils.getDimensionId(level)
        loadedWorlds.add(dimId)
        val fileNames = FileNames(level)
        try {
            readSave(fileNames.worldSave)
        } catch (ex: Exception) {
            try {
                ex.printStackTrace()
                Utils.println("Using BACKUP Electrical Age save: " + fileNames.backupSave)
                readSave(fileNames.backupSave)
            } catch (ex2: Exception) {
                ex2.printStackTrace()
                Utils.println("Failed to read backup save!")
                forWorld(level)
            }
        }
    }

    @Throws(IOException::class)
    private fun readSave(worldSave: Path) {
        if (!Files.exists(worldSave)) return
        val inputStream = ByteArrayInputStream(Files.readAllBytes(worldSave))
        val nbt = NbtIo.readCompressed(inputStream)
        readFromEaWorldNBT(nbt)
    }

    @SubscribeEvent
    fun onWorldUnload(e: LevelEvent.Unload) {
        val level = e.level as? Level ?: return
        if (level.isClientSide) return
        val dimId = Utils.getDimensionId(level)
        loadedWorlds.remove(dimId)
        try {
            NodeManager.instance!!.unload(dimId)
            Eln.ghostManager?.unload(dimId)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @SubscribeEvent
    fun onWorldSave(e: LevelEvent.Save) {
        val level = e.level as? Level ?: return
        if (level.isClientSide) return
        val dimId = Utils.getDimensionId(level)
        if (!loadedWorlds.contains(dimId)) {
            return
        }
        try {
            val nbt = CompoundTag()
            writeToEaWorldNBT(nbt, dimId)
            val fileNames = FileNames(level)

            // Write a new save to a temporary file.
            val bytes = ByteArrayOutputStream(512 * 1024)
            NbtIo.writeCompressed(nbt, bytes)
            Files.write(fileNames.tempSave, bytes.toByteArray())

            // Replace backup save with old save, and old save with new one.
            if (Files.exists(fileNames.worldSave)) replaceFile(fileNames.worldSave, fileNames.backupSave)
            replaceFile(fileNames.tempSave, fileNames.worldSave)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun replaceFile(from: Path, to: Path) {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private inner class FileNames internal constructor(level: Level) {
        val worldSave: Path
        val tempSave: Path
        val backupSave: Path
        
        private fun getEaWorldSaveName(w: Level): String {
            val server = w.server ?: return "electricalAgeWorld_CLIENT_ERROR.dat"
            val dimId = Utils.getDimensionId(w)
            val rootPath = server.getWorldPath(LevelResource.ROOT)
            return rootPath.resolve("data/electricalAgeWorld$dimId.dat").toString()
        }

        init {
            val saveName = getEaWorldSaveName(level)
            worldSave = FileSystems.getDefault().getPath(saveName)
            tempSave = FileSystems.getDefault().getPath("$saveName.tmp")
            backupSave = FileSystems.getDefault().getPath("$saveName.bak")
        }
    }

    companion object {
        fun readFromEaWorldNBT(nbt: CompoundTag) {
            try {
                NodeManager.instance!!.loadFromNbt(nbt.getCompound("nodes"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                Eln.ghostManager?.load(nbt.getCompound("ghost"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun writeToEaWorldNBT(nbt: CompoundTag?, dim: Int) {
            if (nbt == null) return
            try {
                NodeManager.instance!!.saveToNbt(Utils.getOrCreateCompound(nbt, "nodes"), dim)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                Eln.ghostManager?.save(Utils.getOrCreateCompound(nbt, "ghost"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }
}

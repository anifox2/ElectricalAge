@file:Suppress("NAME_SHADOWING")
package mods.eln.ghost

import mods.eln.Eln
import mods.eln.misc.Coordinate
// import mods.eln.misc.Utils.getTags
import mods.eln.node.NodeManager
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.core.BlockPos
import java.util.*
import net.minecraftforge.server.ServerLifecycleHooks

class GhostManager {
    var ghostTable: MutableMap<Coordinate?, GhostElement> = Hashtable()
    var observerTable: MutableMap<Coordinate?, GhostObserver> = Hashtable()
    
    fun clear() {
        ghostTable.clear()
        observerTable.clear()
    }

    fun init() {}
    
    fun isDirty(): Boolean {
        return true
    }

    fun getGhost(coordinate: Coordinate?): GhostElement? {
        return ghostTable[coordinate]
    }

    fun removeGhost(coordinate: Coordinate?) {
        removeGhostNode(coordinate)
        ghostTable.remove(coordinate)
    }

    fun addObserver(observer: GhostObserver) {
        observerTable[observer.ghostObserverCoordonate] = observer
    }

    fun getObserver(coordinate: Coordinate?): GhostObserver? {
        return observerTable[coordinate]
    }

    fun removeObserver(coordinate: Coordinate?) {
        observerTable.remove(coordinate)
    }

    fun removeGhostAndBlockWithObserver(observerCoordinate: Coordinate?) {
        val iterator: MutableIterator<Map.Entry<Coordinate?, GhostElement>> = ghostTable.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val element = entry.value
            if (element.observatorCoordonate!!.equals(observerCoordinate)) {
                iterator.remove()
                removeGhostNode(element.elementCoordinate)
                setBlockToAir(element.elementCoordinate!!)
            }
        }
    }

    fun removeGhostAndBlockWithObserver(observerCoordinate: Coordinate?, uuid: Int) {
        val iterator: MutableIterator<Map.Entry<Coordinate?, GhostElement>> = ghostTable.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val element = entry.value
            if (element.observatorCoordonate!!.equals(observerCoordinate) && element.uUID == uuid) {
                iterator.remove()
                removeGhostNode(element.elementCoordinate)
                setBlockToAir(element.elementCoordinate!!)
            }
        }
    }

    fun removeGhostAndBlockWithObserverAndNotUuid(observerCoordinate: Coordinate?, uuid: Int) {
        val iterator: MutableIterator<Map.Entry<Coordinate?, GhostElement>> = ghostTable.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val element = entry.value
            if (element.observatorCoordonate!!.equals(observerCoordinate) && element.uUID != uuid) {
                iterator.remove()
                removeGhostNode(element.elementCoordinate)
                setBlockToAir(element.elementCoordinate!!)
            }
        }
    }

    fun removeGhostNode(c: Coordinate?) {
        val node = NodeManager.instance!!.getNodeFromCoordonate(c) ?: return
        node.onBreakBlock()
    }

    fun removeGhostAndBlock(coordinate: Coordinate) {
        removeGhost(coordinate)
        setBlockToAir(coordinate)
    }

    fun loadFromNBT(nbt: CompoundTag?) {
        load(nbt)
    }

    fun load(nbt: CompoundTag?) {
        if (nbt == null) return
        for (key in nbt.allKeys) {
            val o = nbt.getCompound(key)
            val ghost = GhostElement()
            ghost.readFromNBT(o, "")
            ghostTable[ghost.elementCoordinate] = ghost
        }
    }

    private fun getTags(nbt: CompoundTag): List<CompoundTag> {
        val list = ArrayList<CompoundTag>()
        for (key in nbt.allKeys) {
            if (key.startsWith("n")) {
                list.add(nbt.getCompound(key))
            }
        }
        return list
    }

    fun save(nbt: CompoundTag) {
        var nodeCounter = 0
        for (ghost in ghostTable.values) {
            // Assuming global save or handled by caller
            val nbtGhost = CompoundTag()
            ghost.writeToNBT(nbtGhost, "")
            nbt.put("n" + nodeCounter++, nbtGhost)
        }
    }

    fun unload(dimensionId: Int) {
        val i = ghostTable.values.iterator()
        while (i.hasNext()) {
            val n = i.next()
            if (n.elementCoordinate!!.dimension == dimensionId) {
                i.remove()
            }
        }
    }

    fun canCreateGhostAt(world: Level, x: Int, y: Int, z: Int): Boolean {
        val pos = BlockPos(x, y, z)
        if (!world.hasChunkAt(pos)) return false
        val state = world.getBlockState(pos)
        return state.isAir || state.canBeReplaced()
    }

    @JvmOverloads
    fun createGhost(coordinate: Coordinate, observerCoordinate: Coordinate, UUID: Int, block: Block? = Eln.ghostBlock.get(), meta: Int = 0) {
        val world = getLevel(coordinate.dimension) ?: return
        val pos = BlockPos(coordinate.x, coordinate.y, coordinate.z)
        
        world.removeBlock(pos, false)
        if (world.setBlock(pos, block!!.defaultBlockState(), 3)) {
            val element = GhostElement(Coordinate(coordinate), observerCoordinate, UUID)
            ghostTable[element.elementCoordinate] = element
        }
    }
    
    private fun setBlockToAir(c: Coordinate) {
        val world = getLevel(c.dimension) ?: return
        world.removeBlock(BlockPos(c.x, c.y, c.z), false)
    }
    
    private fun getLevel(dim: Int): Level? {
        val server = ServerLifecycleHooks.getCurrentServer() ?: return null
        val key = when(dim) {
            0 -> Level.OVERWORLD
            -1 -> Level.NETHER
            1 -> Level.END
            else -> return null // TODO: Support custom dimensions
        }
        return server.getLevel(key)
    }
}

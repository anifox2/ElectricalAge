package mods.eln.node

import mods.eln.misc.Coordinate
import mods.eln.misc.Utils.println
import mods.eln.node.transparent.TransparentNode
import mods.eln.node.transparent.TransparentNodeElement
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData
import java.util.*

class NodeManager : SavedData() {
    val nodeArray: HashMap<Coordinate, NodeBase>
    val nodes: ArrayList<NodeBase>
    val nodeList: Collection<NodeBase>
        get() = nodeArray.values

    fun addNode(node: NodeBase) {
        // nodeArray.add(node);
        val old = nodeArray.put(node.coordinate, node)
        if (old != null) {
            nodes.remove(old)
        }
        nodes.add(node)
        println("NodeManager has " + nodeArray.size + "node")
        // nodeArray.put(new NodeIdentifier(node), node);
        setDirty()
    }

    fun removeNode(node: NodeBase?) {
        if (node == null) return
        nodeArray.remove(node.coordinate)
        nodes.remove(node)
        println("NodeManager has " + nodeArray.size + "node")
        setDirty()
    }

    fun removeCoordonate(c: Coordinate?) {
        // nodeArray.remove(node);
        val n = nodeArray.remove(c)
        if (n != null) nodes.remove(n)
        println("NodeManager has " + nodeArray.size + "node")
        setDirty()
    }

    fun getNodeFromCoordinate(coordinate: Coordinate): NodeBase? {
        return nodeArray[coordinate]
    }

    // override fun isDirty(): Boolean {
    //    return true
    // }

    fun load(nbt: CompoundTag) {}

    override fun save(nbt: CompoundTag): CompoundTag {
        return nbt
    }

    fun getNodeFromCoordonate(nodeCoordinate: Coordinate?): NodeBase? {
        return nodeArray[nodeCoordinate]
    }

    fun getTransparentNodeFromCoordinate(coord: Coordinate?): TransparentNodeElement? {
        val base = getNodeFromCoordonate(coord)
        if (base is TransparentNode) {
            return base.element
        }
        return null
    }

    var rand = Random()
    val randomNode: NodeBase?
        get() = if (nodes.isEmpty()) null else nodes[rand.nextInt(nodes.size)]

    fun loadFromNbt(nbt: CompoundTag?) {
        val addedNode: MutableList<NodeBase> = ArrayList()
        for (key in nbt!!.allKeys) {
            val tag = nbt.getCompound(key)
            val nodeClass = UUIDToClass[tag.getString("tag")]
            try {
                val node = nodeClass!!.getConstructor().newInstance() as NodeBase
                node.readFromNBT(tag)
                addNode(node)
                addedNode.add(node)
                node.initializeFromNBT()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        for (n in addedNode) {
            n.globalBoot()
        }
    }

    fun saveToNbt(nbt: CompoundTag, dim: Int) {
        var nodeCounter = 0
        val nodesCopy: MutableList<NodeBase> = ArrayList()
        nodesCopy.addAll(nodes)
        for (node in nodesCopy) {
            try {
                if (node.mustBeSaved() == false) continue
                if (dim != Int.MIN_VALUE && node.coordinate.dimension != dim) continue
                val nbtNode = CompoundTag()
                nbtNode.putString("tag", node.nodeUuid)
                node.writeToNBT(nbtNode)
                nbt.put("n" + nodeCounter++, nbtNode)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clear() {
        nodes.clear()
        nodeArray.clear()
    }

    fun unload(dimensionId: Int) {
        val i = nodes.iterator()
        while (i.hasNext()) {
            val n = i.next()
            if (n.coordinate.dimension == dimensionId) {
                n.unload()
                i.remove()
                nodeArray.remove(n.coordinate)
            }
        }
    }

    companion object {
        @JvmField
        var instance: NodeManager? = null
        val UUIDToClass = HashMap<String, Class<*>>()
        @JvmStatic
        fun registerUuid(uuid: String, classType: Class<*>) {
            UUIDToClass[uuid] = classType
        }
    }

    init {
        nodeArray = HashMap()
        nodes = ArrayList()
        instance = this
    }
}

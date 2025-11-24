@file:Suppress("NAME_SHADOWING")
package mods.eln.node.six

import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.Direction.Companion.fromInt
import mods.eln.misc.LRDU
import mods.eln.misc.Utils.println
import mods.eln.misc.Utils.updateAllLightTypes
import mods.eln.misc.Utils.updateSkylight
import mods.eln.node.NodeBlockEntity
import net.minecraft.world.level.block.Block
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.Level
import net.minecraft.core.registries.BuiltInRegistries
import java.io.DataInputStream
import java.io.IOException

class SixNodeEntity(pos: net.minecraft.core.BlockPos, state: net.minecraft.world.level.block.state.BlockState) : NodeBlockEntity(Eln.sixNodeEntity!!, pos, state) {
    @JvmField
    var elementRenderList = arrayOfNulls<SixNodeElementRender>(6)
    @JvmField
    var elementRenderIdList = ShortArray(6)
    var sixNodeCacheBlock: Block = Blocks.AIR
    var sixNodeCacheBlockMeta: Byte = 0
    
    override fun serverPublishUnserialize(stream: DataInputStream) {
        val sixNodeCacheBlockOld = sixNodeCacheBlock
        super.serverPublishUnserialize(stream)
        try {
            val blockId = stream.readInt()
            sixNodeCacheBlock = BuiltInRegistries.BLOCK.byId(blockId)
            sixNodeCacheBlockMeta = stream.readByte()
            var idx: Int
            idx = 0
            while (idx < 6) {
                val id = stream.readShort()
                if (id.toInt() == 0) {
                    elementRenderIdList[idx] = 0.toShort()
                    elementRenderList[idx] = null
                } else {
                    if (id != elementRenderIdList[idx]) {
                        var failed = false
                        elementRenderIdList[idx] = id
                        val descriptor = Eln.sixNodeItem!!.getDescriptor(id.toInt()) as? SixNodeDescriptor
                        if (descriptor == null) {
                            println("ERROR: Server sent bad SixNodeDescriptor id $id")
                            failed = true
                        }
                        if (!failed) {
                            try {
                                elementRenderList[idx] = descriptor!!.RenderClass.getConstructor(SixNodeEntity::class.java, Direction::class.java, SixNodeDescriptor::class.java).newInstance(this, fromInt(idx), descriptor) as SixNodeElementRender
                            } catch (e: Exception) {
                                println("ERROR: Initialize SixNodeElementRender for id " + id + " descriptor " + descriptor + " RenderClass " + descriptor!!.RenderClass + " failed with exception " + e)
                                e.printStackTrace()
                                failed = true
                            }
                        }
                        if (failed) {
                            println("ERROR: A previous failure has desynchronized the DataInputStream for this packet. No further information can be processed. If something isn't rendering right now, please post a bug report for this version of Electrical Age.")
                            println("... " + stream.available() + " bytes remained on the stream, consuming all of them")
                            stream.skip(stream.available().toLong())
                            break
                        }
                    }
                    if (elementRenderList[idx] != null) elementRenderList[idx]!!.publishUnserialize(stream)
                }
                idx++
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        if (sixNodeCacheBlock !== sixNodeCacheBlockOld && level != null) {
            updateAllLightTypes(level!!, worldPosition)
        }
    }

    override fun serverPacketUnserialize(stream: DataInputStream) {
        super.serverPacketUnserialize(stream)
        try {
            val side = stream.readByte().toInt()
            val id = stream.readShort().toInt()
            if (elementRenderIdList[side].toInt() == id) {
                elementRenderList[side]!!.serverPacketUnserialize(stream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getSyncronizedSideEnable(direction: Direction): Boolean {
        return elementRenderList[direction.int] != null
    }

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        val n = node as SixNode? ?: return null
        return n.newContainer(side, player)
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen? {
        return elementRenderList[side.int]!!.newGuiDraw(side, player)
    }

    override fun getCableRender(side: Direction, lrdu: LRDU): CableRenderDescriptor? {
        var side = side
        side = side.applyLRDU(lrdu)
        return if (elementRenderList[side.int] == null) null else elementRenderList[side.int]!!.getCableRender(lrdu)
    }

    override fun getCableDry(side: Direction?, lrdu: LRDU?): Int {
        var side = side
        side = side!!.applyLRDU(lrdu!!)
        return if (elementRenderList[side.int] == null) 0 else elementRenderList[side.int]!!.getCableDry(lrdu)
    }

    override fun cameraDrawOptimisation(): Boolean {
        for (e in elementRenderList) {
            if (e != null && !e.cameraDrawOptimisation()) return false
        }
        return true
    }

    override fun destructor() {
        for (render in elementRenderList) {
            render?.destructor()
        }
        super.destructor()
    }

    fun getDamageValue(level: Level, @Suppress("UNUSED_PARAMETER") x: Int, @Suppress("UNUSED_PARAMETER") y: Int, @Suppress("UNUSED_PARAMETER") z: Int): Int {
        if (level.isClientSide) {
            for (idx in 0..5) {
                if (elementRenderList[idx] != null) {
                    return elementRenderIdList[idx].toInt()
                }
            }
        }
        return 0
    }

    fun hasVolume(level: net.minecraft.world.level.BlockGetter, pos: net.minecraft.core.BlockPos): Boolean {
        if (this.level != null && !this.level!!.isClientSide) {
            val node = node as SixNode?
            if (node != null) {
                return node.hasVolume()
            }
        }
        for (e in elementRenderList) {
            if (e != null && e.sixNodeDescriptor.hasVolume()) return true
        }
        return false
    }

    override fun tileEntityNeighborSpawn() {
        for (e in elementRenderList) {
            e?.notifyNeighborSpawn()
        }
    }

    override val nodeUuid: String
        get() = Eln.sixNodeBlock!!.nodeUuid

    override fun clientRefresh(deltaT: Float) {
        for (e in elementRenderList) {
            e?.refresh(deltaT)
        }
    }

    override fun isProvidingWeakPower(side: Direction?): Int {
        return if (level?.isClientSide == true) {
            var max = 0
            for (r in elementRenderList) {
                if (r == null) continue
                if (max < r.isProvidingWeakPower(side)) max = r.isProvidingWeakPower(side)
            }
            max
        } else {
            if (node == null) 0 else node!!.isProvidingWeakPower(side)
        }
    }

    override fun onBlockPlacedBy(front: Direction?, entityLiving: net.minecraft.world.entity.LivingEntity?, stack: net.minecraft.world.item.ItemStack) {
        if (level!!.isClientSide) return
        
        val node = SixNode()
        node.coordinate = Coordinate(this)
        node.level = level
        
        mods.eln.node.NodeManager.instance!!.addNode(node)
        this.internalNode = node
        
        node.onBlockPlacedBy(level!!, Coordinate(this), front ?: Direction.N, entityLiving, stack)
    }

    fun hasElement(side: Direction): Boolean {
        if (level?.isClientSide == true) {
            return elementRenderList[side.int] != null
        } else {
            val n = node as? SixNode ?: return false
            return n.sideElementList[side.int] != null
        }
    }

    companion object {
        const val singleTargetId = 2
    }

    init {
        for (idx in 0..5) {
            elementRenderList[idx] = null
            elementRenderIdList[idx] = 0
        }
    }
}

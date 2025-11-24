package mods.eln.node

import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Utils
import mods.eln.misc.Utils.fatal
import mods.eln.misc.Utils.notifyNeighbor
import mods.eln.misc.Utils.println
import mods.eln.misc.UtilsClient
import mods.eln.server.DelayedBlockRemove.Companion.add
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.level.LightLayer
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.util.concurrent.LinkedBlockingQueue

abstract class NodeBlockEntity(type: net.minecraft.world.level.block.entity.BlockEntityType<*>, pos: net.minecraft.core.BlockPos, state: net.minecraft.world.level.block.state.BlockState) : BlockEntity(type, pos, state), ITileEntitySpawnClient, INodeEntity {

    val block: NodeBlock
        get() = blockState.block as NodeBlock
    var redstone = false
    var lastLight = 0xFF
    var firstUnserialize = true
    override fun serverPublishUnserialize(stream: DataInputStream) {
        var light = 0
        try {
            if (firstUnserialize) {
                firstUnserialize = false
                level!!.updateNeighborsAt(worldPosition, blockState.block)
            }
            val b = stream.readByte()
            light = b.toInt() and 0xF
            val newRedstone = b.toInt() and 0x10 != 0
            if (redstone != newRedstone) {
                redstone = newRedstone
                level!!.sendBlockUpdated(worldPosition, blockState, blockState, 3)
            } else {
                redstone = newRedstone
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        /*	if(lastLight == 0xFF) //boot trololol
        {
			lastLight = 15;
			level.updateLightByType(EnumSkyBlock.Block,xCoord,yCoord,zCoord);
		}*/if (lastLight != light) {
            lastLight = light
            level!!.chunkSource.lightEngine.checkBlock(worldPosition)
        }
    }

    override fun serverPacketUnserialize(stream: DataInputStream) {}

    abstract fun isProvidingWeakPower(side: Direction?): Int

    var internalNode: Node? = null

    val node: Node?
        get() {
            if (level?.isClientSide == true) {
                fatal("NodeBlockEntity client side access")
            }
            if (internalNode == null) {
                if (level == null) return null
                val nodeFromCoordonate = NodeManager.instance?.getNodeFromCoordonate(Coordinate(this))
                if (nodeFromCoordonate is Node) {
                    internalNode = nodeFromCoordonate
                } else {
                    // println("ASSERT WRONG TYPE public Node getNode " + Coordinate(this))
                }
                if (internalNode == null) {
                    // Utils.println("This is actually used?")
                    // add(Coordinate(this))
                }
            }
            return internalNode
        }

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return null
    }

    @OnlyIn(Dist.CLIENT)
    override fun newGuiDraw(side: Direction, player: Player): Screen? {
        // Debugging tip: If the GUI isn't working, but you can see it trying to open in the client debug log,
        // check that you have the renderer (client) class set correctly in the descriptor
        return null
    }

    @OnlyIn(Dist.CLIENT)
    override fun getRenderBoundingBox(): AABB {
        return if (cameraDrawOptimisation()) {
            AABB((worldPosition.x - 1).toDouble(), (worldPosition.y - 1).toDouble(), (worldPosition.z - 1).toDouble(), (worldPosition.x + 1).toDouble(), (worldPosition.y + 1).toDouble(), (worldPosition.z + 1).toDouble())
        } else {
            INFINITE_EXTENT_AABB
        }
    }

    open fun cameraDrawOptimisation(): Boolean {
        return true
    }

    val lightValue: Int
        get() = if (level!!.isClientSide) {
            if (lastLight == 0xFF) {
                0
            } else lastLight
        } else {
            node?.lightValue?: 0
        }

    override fun load(tag: CompoundTag) {
        super.load(tag)
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
    }

    //max draw distance
    // @SideOnly(Side.CLIENT)
    // override fun getMaxRenderDistanceSquared(): Double {
    //    return 4096.0 * 4 * 4
    // }

    open fun onBlockPlacedBy(front: Direction?, entityLiving: LivingEntity?, stack: net.minecraft.world.item.ItemStack) {}
    open fun tick() {
        if (level!!.isClientSide) return
        if (redstone) {
            level!!.removeBlock(worldPosition, false)
            redstone = false
        }
    }

    fun onBlockAdded() {
        if (!level!!.isClientSide && node == null) {
            // level!!.removeBlock(worldPosition, false)
        }
    }

    fun onBreakBlock() {
        if (!level!!.isClientSide) {
            if (node == null) return
            node!!.onBreakBlock()
        }
    }

    override fun onChunkUnloaded() {
        if (!level!!.isClientSide) {
            node?.onChunkUnload()
        }
    }

    /**
     * invalidates a tile entity
     */
    override fun setRemoved() {
        if (!level!!.isClientSide) {
            node?.onBreakBlock()
        } else {
            destructor()
        }
        super.setRemoved()
    }

    //client only
    open fun destructor() {
        clientList.remove(this)
    }

    // override fun invalidate() {
    //    if (level.isRemote) {
    //        destructor()
    //    }
    //    super.invalidate()
    // }

    fun onBlockActivated(entityPlayer: Player?, side: Direction?, vx: Float, vy: Float, vz: Float): Boolean {
        if (!level!!.isClientSide) {
            if (node == null) return false
            node!!.onBlockActivated(entityPlayer!!, side!!, vx, vy, vz)
            return true
        }
        //if(entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock)
        run { return true }
        //return true;
    }

    fun onNeighborBlockChange() {
        if (!level!!.isClientSide) {
            if (node == null) return
            node!!.onNeighborBlockChange()
        }
    }

    override fun getUpdatePacket(): Packet<net.minecraft.network.protocol.game.ClientGamePacketListener>? {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(): CompoundTag {
        val tag = super.getUpdateTag()
        if (level != null && !level!!.isClientSide) {
            val node = node
            if (node != null && node.publishPacket != null) {
                tag.putByteArray("elnData", node.publishPacket!!.toByteArray())
            }
        }
        return tag
    }

    override fun onDataPacket(net: net.minecraft.network.Connection, pkt: net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket) {
        val tag = pkt.tag
        if (tag != null && tag.contains("elnData")) {
            val data = tag.getByteArray("elnData")
            val stream = DataInputStream(java.io.ByteArrayInputStream(data))
            try {
                // Skip header written by NodeBase.publishPacket
                stream.readByte() // packetId
                stream.readInt() // x
                stream.readInt() // y
                stream.readInt() // z
                stream.readByte() // dim
                stream.readUTF() // uuid
                
                serverPublishUnserialize(stream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    open fun preparePacketForServer(stream: DataOutputStream) {
        try {
            stream.writeByte(Eln.packetPublishForNode.toInt())
            stream.writeInt(worldPosition.x)
            stream.writeInt(worldPosition.y)
            stream.writeInt(worldPosition.z)
            stream.writeByte(0) // TODO: Fix dimension ID
            stream.writeUTF(nodeUuid)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    open fun sendPacketToServer(bos: ByteArrayOutputStream?) {
        UtilsClient.sendPacketToServer(bos!!)
    }

    open fun getCableRender(side: Direction, lrdu: LRDU): CableRenderDescriptor? {
        return null
    }

    open fun getCableDry(side: Direction?, lrdu: LRDU?): Int {
        return 0
    }

    fun canConnectRedstone(@Suppress("UNUSED_PARAMETER") xn: Direction?): Boolean {
        return if (level!!.isClientSide) redstone else {
            if (node == null) false else node!!.canConnectRedstone()
        }
    }

    open fun clientRefresh(deltaT: Float) {}

    companion object {
        @JvmField
        //val clientList = LinkedList<NodeBlockEntity>()
        val clientList = LinkedBlockingQueue<NodeBlockEntity>()
        fun getEntity(x: Int, y: Int, z: Int): NodeBlockEntity? {
            var entity: BlockEntity?
            val pos = net.minecraft.core.BlockPos(x, y, z)
            val level = Minecraft.getInstance().level ?: return null
            if (level.getBlockEntity(pos).also { entity = it } != null) {
                if (entity is NodeBlockEntity) {
                    return entity as NodeBlockEntity?
                }
            }
            return null
        }
    }
}

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
import net.minecraft.client.gui.Screen
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Packet
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.level.LightLayer
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

abstract class NodeBlockEntity(type: net.minecraft.world.level.block.entity.BlockEntityType<*>, pos: net.minecraft.core.BlockPos, state: net.minecraft.world.level.block.state.BlockState) : BlockEntity(type, pos, state), ITileEntitySpawnClient, INodeEntity {

    val block: NodeBlock
        get() = getBlockType() as NodeBlock
    var redstone = false
    var lastLight = 0xFF
    var firstUnserialize = true
    override fun serverPublishUnserialize(stream: DataInputStream) {
        var light = 0
        try {
            if (firstUnserialize) {
                firstUnserialize = false
                notifyNeighbor(this)
            }
            val b = stream.readByte()
            light = b.toInt() and 0xF
            val newRedstone = b.toInt() and 0x10 != 0
            if (redstone != newRedstone) {
                redstone = newRedstone
                level.notifyBlockChange(xCoord, yCoord, zCoord, getBlockType())
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
            level.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord)
        }
    }

    override fun serverPacketUnserialize(stream: DataInputStream) {}

    abstract fun isProvidingWeakPower(side: Direction?): Int

    var internalNode: Node? = null

    val node: Node?
        get() {
            if (level.isRemote) {
                fatal()
            }
            if (internalNode == null) {
                val nodeFromCoordonate = NodeManager.instance!!.getNodeFromCoordonate(Coordinate(xCoord, yCoord, zCoord, level))
                if (nodeFromCoordonate is Node) {
                    internalNode = nodeFromCoordonate
                } else {
                    println("ASSERT WRONG TYPE public Node getNode " + Coordinate(xCoord, yCoord, zCoord, level))
                }
                if (internalNode == null) {
                    Utils.println("This is actually used?")
                    add(Coordinate(xCoord, yCoord, zCoord, level))
                }
            }
            return internalNode
        }

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return null
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen? {
        // Debugging tip: If the GUI isn't working, but you can see it trying to open in the client debug log,
        // check that you have the renderer (client) class set correctly in the descriptor
        return null
    }

    @SideOnly(Side.CLIENT)
    override fun getRenderBoundingBox(): AABB {
        return if (cameraDrawOptimisation()) {
            AABB.getBoundingBox((xCoord - 1).toDouble(), (yCoord - 1).toDouble(), (zCoord - 1).toDouble(), (xCoord + 1).toDouble(), (yCoord + 1).toDouble(), (zCoord + 1).toDouble())
        } else {
            INFINITE_EXTENT_AABB
        }
    }

    open fun cameraDrawOptimisation(): Boolean {
        return true
    }

    val lightValue: Int
        get() = if (level.isRemote) {
            if (lastLight == 0xFF) {
                0
            } else lastLight
        } else {
            node?.lightValue?: 0
        }

    /**
     * Reads a tile entity from NBT.
     */
    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
    }

    /**
     * Writes a tile entity to NBT.
     */
    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
    }

    //max draw distance
    @SideOnly(Side.CLIENT)
    override fun getMaxRenderDistanceSquared(): Double {
        return 4096.0 * 4 * 4
    }

    @Suppress("UNUSED_PARAMETER") fun onBlockPlacedBy(front: Direction?, entityLiving: LivingEntity?, metadata: Int) {}
    override fun canUpdate(): Boolean {
        return true
    }

    var updateEntityFirst = true
    override fun updateEntity() {
        if (updateEntityFirst) {
            updateEntityFirst = false
            if (!level.isRemote) {
                // level.setBlock(xCoord, yCoord, zCoord, 0);
            } else {
                clientList.add(this)
            }
        }
    }

    fun onBlockAdded() {
        if (!level.isRemote && node == null) {
            level.setBlockToAir(xCoord, yCoord, zCoord)
        }
    }

    fun onBreakBlock() {
        if (!level.isRemote) {
            if (node == null) return
            node!!.onBreakBlock()
        }
    }

    override fun onChunkUnload() {
        if (level.isRemote) {
            destructor()
        }
    }

    //client only
    open fun destructor() {
        clientList.remove(this)
    }

    override fun invalidate() {
        if (level.isRemote) {
            destructor()
        }
        super.invalidate()
    }

    fun onBlockActivated(entityPlayer: Player?, side: Direction?, vx: Float, vy: Float, vz: Float): Boolean {
        if (!level.isRemote) {
            if (node == null) return false
            node!!.onBlockActivated(entityPlayer!!, side!!, vx, vy, vz)
            return true
        }
        //if(entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock)
        run { return true }
        //return true;
    }

    fun onNeighborBlockChange() {
        if (!level.isRemote) {
            if (node == null) return
            node!!.onNeighborBlockChange()
        }
    }

    override fun getDescriptionPacket(): Packet? {
        val node = node //TO DO NULL POINTER
        if (node == null) {
            println("ASSERT NULL NODE public Packet getDescriptionPacket() nodeblock entity")
            return null
        }
        return S3FPacketCustomPayload(Eln.channelName, node.publishPacket!!.toByteArray())
    }

    open fun preparePacketForServer(stream: DataOutputStream) {
        try {
            stream.writeByte(Eln.packetPublishForNode.toInt())
            stream.writeInt(xCoord)
            stream.writeInt(yCoord)
            stream.writeInt(zCoord)
            stream.writeByte(level.provider.dimensionId)
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
        return if (level.isRemote) redstone else {
            if (node == null) false else node!!.canConnectRedstone()
        }
    }

    open fun clientRefresh(deltaT: Float) {}

    companion object {
        @JvmField
        //val clientList = LinkedList<NodeBlockEntity>()
        val clientList = LinkedBlockingQueue<NodeBlockEntity>()
        fun getEntity(x: Int, y: Int, z: Int): NodeBlockEntity? {
            var entity: TileEntity?
            if (Minecraft.getMinecraft().theWorld.getTileEntity(x, y, z).also { entity = it } != null) {
                if (entity is NodeBlockEntity) {
                    return entity as NodeBlockEntity?
                }
            }
            return null
        }
    }
}

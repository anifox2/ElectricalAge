package mods.eln.node.simple

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.Direction.Companion.fromInt
import mods.eln.misc.Utils.fatal
import mods.eln.misc.Utils.println
import mods.eln.node.INodeEntity
import mods.eln.node.NodeEntityClientSender
import mods.eln.node.NodeManager
import mods.eln.node.simple.DescriptorManager.get
import mods.eln.server.DelayedBlockRemove.Companion.add
import net.minecraft.client.gui.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraft.tileentity.TileEntity
import java.io.DataInputStream
import java.io.IOException
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

abstract class SimpleNodeEntity(override val nodeUuid: String, pos: BlockPos, state: BlockState) : BlockEntity(null!!, pos, state), INodeEntity {
    open var node: SimpleNode? = null
        get() {
            if (level!!.isClientSide) {
                fatal()
                return null
            }
            if (level == null) return null
            if (field == null) {
                field = NodeManager.instance!!.getNodeFromCoordonate(Coordinate(blockPos.x, blockPos.y, blockPos.z, level!!)) as SimpleNode?
                if (field == null) {
                    add(Coordinate(blockPos.x, blockPos.y, blockPos.z, level!!))
                    return null
                }
            }
            return field
        }

    //***************** Wrapping **************************
    /*
	public void onBlockPlacedBy(Direction front, LivingEntity entityLiving, int metadata) {
	
	}
*/
    fun onBlockAdded() {
        /*if (!level.isRemote){
			if (getNode() == null) {
				level.setBlockToAir(xCoord, yCoord, zCoord);
			}
		}*/
    }

    fun onBreakBlock() {
        if (!level.isRemote) {
            if (node == null) return
            node!!.onBreakBlock()
        }
    }

    override fun onChunkUnload() {
        super.onChunkUnload()
        if (level.isRemote) {
            destructor()
        }
    }

    // client only
    fun destructor() {}
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
        return true
    }

    fun onNeighborBlockChange() {
        if (!level.isRemote) {
            if (node == null) return
            node!!.onNeighborBlockChange()
        }
    }

    //***************** Descriptor **************************
    val descriptor: Any?
        get() {
            val b = getBlockType() as SimpleNodeBlock
            return get<Any>(b.descriptorKey)
        }

    //***************** Network **************************
    var front: Direction? = null
    override fun serverPublishUnserialize(stream: DataInputStream) {
        try {
            if (front !== fromInt(stream.readByte().toInt()).also { front = it }) {
                level.markBlockForUpdate(xCoord, yCoord, zCoord)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun serverPacketUnserialize(stream: DataInputStream) {}
    override fun getDescriptionPacket(): Packet? {
        val node = node
        if (node == null) {
            println("ASSERT NULL NODE public Packet getDescriptionPacket() nodeblock entity")
            return null
        }
        return S3FPacketCustomPayload(Eln.channelName, node.publishPacket!!.toByteArray())
    }

    open lateinit var sender: NodeEntityClientSender

    init {
        println("NodeUUID: $nodeUuid")
        sender = NodeEntityClientSender(this, nodeUuid)
    }

    //*********************** GUI ***************************
    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return null
    }

    @SideOnly(Side.CLIENT)
    override fun newGuiDraw(side: Direction, player: Player): Screen? {
        return null
    }
}

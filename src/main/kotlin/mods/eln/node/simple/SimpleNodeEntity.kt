package mods.eln.node.simple

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.Direction.Companion.fromIntMinecraftSide
import mods.eln.misc.Utils.fatal
import mods.eln.misc.Utils.println
import mods.eln.node.INodeEntity
import mods.eln.node.NodeEntityClientSender
import mods.eln.node.NodeManager
import mods.eln.node.simple.DescriptorManager.get
import mods.eln.server.DelayedBlockRemove.Companion.add
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.nbt.CompoundTag
import java.io.DataInputStream
import java.io.IOException

abstract class SimpleNodeEntity(type: BlockEntityType<*>, override val nodeUuid: String, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state), INodeEntity {
    open var node: SimpleNode? = null
        get() {
            if (level!!.isClientSide) {
                fatal("SimpleNodeEntity client side access")
                return null
            }
            if (level == null) return null
            if (field == null) {
                field = NodeManager.instance!!.getNodeFromCoordonate(Coordinate(this)) as SimpleNode?
                if (field == null) {
                    add(Coordinate(this))
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
        if (!level!!.isClientSide && node == null) {
            level!!.removeBlock(worldPosition, false)
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

    override fun setRemoved() {
        if (!level!!.isClientSide) {
            node?.onBreakBlock()
        }
        super.setRemoved()
    }

    override fun getUpdatePacket(): Packet<net.minecraft.network.protocol.game.ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(): CompoundTag {
        val tag = super.getUpdateTag()
        val node = node
        if (node != null && node.publishPacket != null) {
             tag.putByteArray("elnData", node!!.publishPacket!!.toByteArray())
        }
        return tag
    }

    override fun onDataPacket(net: net.minecraft.network.Connection, pkt: ClientboundBlockEntityDataPacket) {
        val tag = pkt.tag
        if (tag != null && tag.contains("elnData")) {
            val data = tag.getByteArray("elnData")
            val stream = DataInputStream(java.io.ByteArrayInputStream(data))
            serverPublishUnserialize(stream)
        }
    }
    
    // ... rest of the file ...

    fun onNeighborBlockChange() {
        if (!level!!.isClientSide) {
            if (node == null) return
            node!!.onNeighborBlockChange()
        }
    }

    fun onBlockActivated(player: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        return node?.onBlockActivated(player, side, vx, vy, vz) ?: false
    }

    //***************** Descriptor **************************
    val descriptor: Any?
        get() {
            val b = blockState.block as SimpleNodeBlock
            return get<Any>(b.descriptorKey)
        }

    //***************** Network **************************
    var front: Direction? = null
    override fun serverPublishUnserialize(stream: DataInputStream) {
        try {
            val newFront = fromIntMinecraftSide(stream.readByte().toInt())
            if (front != newFront) {
                front = newFront
                level!!.sendBlockUpdated(worldPosition, blockState, blockState, 3)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun serverPacketUnserialize(stream: DataInputStream) {}
    
    // getDescriptionPacket replaced by getUpdatePacket

    open lateinit var sender: NodeEntityClientSender

    init {
        println("NodeUUID: $nodeUuid")
        sender = NodeEntityClientSender(this, nodeUuid)
    }

    //*********************** GUI ***************************
    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return null
    }

    // @OnlyIn(Dist.CLIENT)
    override fun newGuiDraw(side: Direction, player: Player): Screen? {
        return null
    }
}

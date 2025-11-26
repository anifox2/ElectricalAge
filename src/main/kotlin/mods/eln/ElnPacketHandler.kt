package mods.eln

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.simple.SimpleChannel
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.PacketDistributor
import java.util.function.Supplier
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import mods.eln.misc.Utils
import mods.eln.misc.Coordinate
import mods.eln.node.NodeManager
import mods.eln.client.ClientProxy
import mods.eln.sound.SoundClient
import mods.eln.sound.SoundCommand
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraft.core.BlockPos
import mods.eln.node.INodeEntity
import net.minecraft.world.level.Level
import mods.eln.misc.Utils.println
import mods.eln.misc.Utils.sendPacketToClient
import mods.eln.ServerKeyHandler

object ElnNetwork {
    private val PROTOCOL = "1"
    val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation("eln", "main"),
        { PROTOCOL }, { it == PROTOCOL }, { it == PROTOCOL }
    )

    fun init() {
        var id = 0
        CHANNEL.registerMessage(id++, ElnPacket::class.java,
            ElnPacket::encode,
            ElnPacket::decode,
            ElnPacket::handle
        )
    }

    fun sendToClient(packet: ElnPacket, player: ServerPlayer) {
        CHANNEL.send(PacketDistributor.PLAYER.with { player }, packet)
    }

    fun sendToServer(packet: ElnPacket) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), packet)
    }
}

class ElnPacket(val data: ByteArray) {
    companion object {
        fun encode(msg: ElnPacket, buf: FriendlyByteBuf) {
            buf.writeByteArray(msg.data)
        }

        fun decode(buf: FriendlyByteBuf): ElnPacket {
            return ElnPacket(buf.readByteArray())
        }

        fun handle(msg: ElnPacket, ctxSupplier: Supplier<NetworkEvent.Context>) {
            val ctx = ctxSupplier.get()
            ctx.enqueueWork {
                if (ctx.direction.receptionSide.isServer) {
                    val player = ctx.sender
                    if (player != null) {
                        ElnPacketHandler.packetRx(DataInputStream(ByteArrayInputStream(msg.data)), player)
                    }
                } else {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT) {
                        Runnable {
                            val player = net.minecraft.client.Minecraft.getInstance().player
                            if (player != null) {
                                ElnPacketHandler.packetRx(DataInputStream(ByteArrayInputStream(msg.data)), player)
                            }
                        }
                    }
                }
            }
            ctx.setPacketHandled(true)
        }
    }
}

object ElnPacketHandler {

    fun packetRx(stream: DataInputStream, player: Player) {
        try {
            val packetId: Byte = stream.readByte()
            when (packetId) {
                Eln.packetPlayerKey.toByte() -> packetPlayerKey(stream, player)
                Eln.packetNodeSingleSerialized.toByte() -> packetNodeSingleSerialized(stream, player)
                Eln.packetPublishForNode.toByte() -> packetForNode(stream, player)
                Eln.packetForClientNode.toByte() -> packetForClientNode(stream, player)
                Eln.packetOpenLocalGui.toByte() -> packetOpenLocalGui(stream, player)
                Eln.packetPlaySound.toByte() -> packetPlaySound(stream, player)
                Eln.packetDestroyUuid.toByte() -> packetDestroyUuid(stream, player)
                Eln.packetClientToServerConnection.toByte() -> packetNewClient(player)
                Eln.packetServerToClientInfo.toByte() -> packetServerInfo(stream, player)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun packetNewClient(player: Player) {
        val bos = ByteArrayOutputStream(64)
        val stream = DataOutputStream(bos)
        try {
            stream.writeByte(Eln.packetServerToClientInfo.toInt())
            for (c in Eln.instance!!.configShared) {
                c.serializeConfig(stream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        sendPacketToClient(bos, player as ServerPlayer)
    }

    private fun packetServerInfo(stream: DataInputStream, @Suppress("UNUSED_PARAMETER") player: Player) {
        for (c in Eln.instance!!.configShared) {
            try {
                c.deserialize(stream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun packetDestroyUuid(stream: DataInputStream, @Suppress("UNUSED_PARAMETER") player: Player) {
        try {
            ClientProxy.uuidManager.kill(stream.readInt())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun packetPlaySound(stream: DataInputStream, player: Player) {
        try {
            val dim = stream.readUTF()
            val playerDim = player.level().dimension().location().toString()
            if (dim != playerDim) return

            SoundClient.play(SoundCommand.fromStream(stream, player.level()))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun packetOpenLocalGui(stream: DataInputStream, player: Player) {
        try {
            val guiId = stream.readInt()
            val x = stream.readInt()
            val y = stream.readInt()
            val z = stream.readInt()

            player.openGuiCompat(Eln.instance!!, guiId, player.level(), x, y, z)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun packetForNode(stream: DataInputStream, player: Player?) {
        try {
            val coordinate = Coordinate(
                stream.readInt(),
                stream.readInt(),
                stream.readInt(),
                stream.readByte().toInt()
            )
            val node = NodeManager.instance?.getNodeFromCoordonate(coordinate)
            if (node != null && node.nodeUuid == stream.readUTF()) {
                node.networkUnserialize(stream, player as? ServerPlayer)
            } else {
                println("packetForNode node not found")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun packetForClientNode(stream: DataInputStream, player: Player) {
        try {
            val x = stream.readInt()
            val y = stream.readInt()
            val z = stream.readInt()
            val dimension = stream.readByte().toInt()
            // TODO: use the dimension value once multi-dimension support is ported.

            val level = player.level()
            val entity = level.getBlockEntity(BlockPos(x, y, z))
            if (entity is INodeEntity) {
                val node = entity as INodeEntity
                if (node.nodeUuid == stream.readUTF()) {
                    node.serverPacketUnserialize(stream)
                    if (stream.available() != 0) {
                        println("0 != stream.available()")
                    }
                } else {
                    println("Wrong node UUID warning")
                    val dataSkipLength = stream.readByte().toInt()
                    for (idx in 0 until dataSkipLength) {
                        stream.readByte()
                    }
                }
            } else {
                println("No node found for $x $y $z")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun packetNodeSingleSerialized(stream: DataInputStream, player: Player) {
        try {
            val x = stream.readInt()
            val y = stream.readInt()
            val z = stream.readInt()
            val dimension = stream.readByte().toInt()
            // TODO: use the dimension value once multi-dimension support is ported.

            val level = player.level()
            val entity = level.getBlockEntity(BlockPos(x, y, z))
            if (entity is INodeEntity) {
                val node = entity as INodeEntity
                if (node.nodeUuid == stream.readUTF()) {
                    node.serverPublishUnserialize(stream)
                    if (stream.available() != 0) {
                        println("0 != stream.available()")
                    }
                } else {
                    println("Wrong node UUID warning")
                    val dataSkipLength = stream.readByte().toInt()
                    for (idx in 0 until dataSkipLength) {
                        stream.readByte()
                    }
                }
            } else {
                println("No node found for $x $y $z")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun packetPlayerKey(stream: DataInputStream, @Suppress("UNUSED_PARAMETER") player: Player?) {
        try {
            val name = stream.readUTF()
            val state = stream.readBoolean()
            ServerKeyHandler.set(name, state)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun Player.openGuiCompat(modInstance: Any, guiId: Int, level: Level, x: Int, y: Int, z: Int) {
        throw UnsupportedOperationException("Player.openGuiCompat is not yet implemented for the new GUI system")
    }
}

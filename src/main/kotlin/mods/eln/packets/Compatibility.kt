package mods.eln.packets

import io.netty.buffer.ByteBuf
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.item.ItemStack

interface IMessage {
    fun fromBytes(buf: ByteBuf?)
    fun toBytes(buf: ByteBuf?)
}

interface IMessageHandler<REQ : IMessage?, REPLY : IMessage?> {
    fun onMessage(message: REQ, ctx: MessageContext?): REPLY
}

class MessageContext {
    var serverHandler: ServerHandler = ServerHandler()
}

class ServerHandler {
    var playerEntity: net.minecraft.world.entity.player.Player? = null
}

object ByteBufUtils {
    fun readUTF8String(buf: ByteBuf?): String {
        if (buf == null) return ""
        return FriendlyByteBuf(buf).readUtf()
    }

    fun writeUTF8String(buf: ByteBuf?, str: String?) {
        if (buf == null) return
        FriendlyByteBuf(buf).writeUtf(str ?: "")
    }

    fun readVarInt(buf: ByteBuf?, size: Int): Int {
        if (buf == null) return 0
        return FriendlyByteBuf(buf).readVarInt()
    }

    fun writeVarInt(buf: ByteBuf?, value: Int, size: Int) {
        if (buf == null) return
        FriendlyByteBuf(buf).writeVarInt(value)
    }

    fun readItemStack(buf: ByteBuf?): ItemStack {
        if (buf == null) return ItemStack.EMPTY
        return FriendlyByteBuf(buf).readItem()
    }

    fun writeItemStack(buf: ByteBuf?, stack: ItemStack?) {
        if (buf == null) return
        FriendlyByteBuf(buf).writeItem(stack ?: ItemStack.EMPTY)
    }
}

object WailaCache {
    // Dummy object
}

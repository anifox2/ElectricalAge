package mods.eln.sixnode.modbusrtu

import mods.eln.misc.INBTTReady
import net.minecraft.nbt.CompoundTag
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

open class WirelessRxStatus : INBTTReady {
    var name: String = ""
    var id: Int = 0
    var uuid: Int = 0
    var connected: Boolean = false

    constructor(name: String, id: Int, connected: Boolean, uuid: Int) {
        this.id = id
        this.name = name
        this.connected = connected
        this.uuid = uuid
    }

    constructor()

    @Throws(IOException::class)
    fun writeTo(packet: DataOutputStream) {
        packet.writeInt(uuid)
        packet.writeInt(id)
        packet.writeUTF(name)
        packet.writeBoolean(connected)
    }

    @Throws(IOException::class)
    fun readFrom(stream: DataInputStream) {
        uuid = stream.readInt()
        id = stream.readInt()
        name = stream.readUTF()
        connected = stream.readBoolean()
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        name = nbt.getString(str + "name")
        id = nbt.getInt(str + "id")
        connected = nbt.getBoolean(str + "connected")
        uuid = nbt.getInt(str + "uuid")
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        nbt.putString(str + "name", name)
        nbt.putInt(str + "id", id)
        nbt.putBoolean(str + "connected", connected)
        nbt.putInt(str + "uuid", uuid)
    }
}

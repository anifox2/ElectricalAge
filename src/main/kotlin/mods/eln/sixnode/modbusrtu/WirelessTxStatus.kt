package mods.eln.sixnode.modbusrtu

import mods.eln.misc.INBTTReady
import net.minecraft.nbt.CompoundTag
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

open class WirelessTxStatus : INBTTReady {
    open var name: String = ""
    var id: Int = 0
    @JvmField var value: Double = 0.0
    var uuid: Int = 0

    constructor()

    constructor(name: String, id: Int, value: Double, uuid: Int) {
        this.id = id
        this.name = name
        this.value = value
        this.uuid = uuid
    }

    open fun getValue(): Double {
        return value
    }

    @Throws(IOException::class)
    fun writeTo(packet: DataOutputStream) {
        packet.writeInt(uuid)
        packet.writeInt(id)
        packet.writeUTF(name)
        packet.writeDouble(value)
    }

    @Throws(IOException::class)
    fun readFrom(stream: DataInputStream) {
        uuid = stream.readInt()
        id = stream.readInt()
        name = stream.readUTF()
        value = stream.readDouble()
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        name = nbt.getString(str + "name")
        id = nbt.getInt(str + "id")
        value = nbt.getDouble(str + "value")
        uuid = nbt.getInt(str + "uuid")
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        nbt.putString(str + "name", name)
        nbt.putInt(str + "id", id)
        nbt.putDouble(str + "value", value)
        nbt.putInt(str + "uuid", uuid)
    }
}

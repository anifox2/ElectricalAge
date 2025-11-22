package mods.eln.misc

import mods.eln.Eln
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.AABB
import net.minecraft.world.level.Level
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

object Utils {
    @JvmStatic
    fun println(message: Any?) {
        Eln.LOGGER.info(message.toString())
    }

    @JvmStatic
    var uuid = 0
        get() = field++
        private set

    @JvmStatic
    fun plotVolt(prefix: String, value: Double): String {
        return "$prefix${String.format("%.2f", value)}V"
    }

    @JvmStatic
    fun plotVolt(value: Double): String {
        return plotValue(value, "V")
    }

    @JvmStatic
    fun plotAmpere(prefix: String, value: Double): String {
        return "$prefix${String.format("%.2f", value)}A"
    }

    @JvmStatic
    fun plotAmpere(value: Double): String {
        return plotValue(value, "A")
    }

    @JvmStatic
    fun plotSignal(value: Double): String {
        return "${String.format("%.2f", value)}"
    }

    @JvmStatic
    fun limit(value: Double, min: Double, max: Double): Double {
        return value.coerceIn(min, max)
    }

    @JvmStatic
    fun canPutStackInInventory(stacks: Array<ItemStack>, inventory: net.minecraft.world.Container, slots: IntArray): Boolean {
        // Stub implementation
        return true
    }

    @JvmStatic
    fun tryPutStackInInventory(stacks: Array<ItemStack>, inventory: net.minecraft.world.Container, slots: IntArray): Boolean {
        // Stub implementation
        return true
    }

    @JvmStatic
    fun readFromNBT(nbt: net.minecraft.nbt.CompoundTag, key: String, obj: Any?) {
        if (obj is INBTTReady) {
            obj.readFromNBT(nbt, key)
        }
    }

    @JvmStatic
    fun writeToNBT(nbt: net.minecraft.nbt.CompoundTag, key: String, obj: Any?) {
        if (obj is INBTTReady) {
            obj.writeToNBT(nbt, key)
        }
    }

    @JvmStatic
    fun addChatMessage(player: net.minecraft.world.entity.player.Player, message: String) {
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message))
    }

    @JvmStatic
    fun nullCheck(obj: Any?) {
        if (obj == null) throw RuntimeException("Null check failed")
    }

    @JvmStatic
    fun plotValue(value: Double): String {
        return String.format("%.2f", value)
    }

    @JvmStatic
    fun plotValue(value: Double, unit: String): String {
        return "${String.format("%.2f", value)}$unit"
    }

    @JvmStatic
    fun plotPercent(value: Double): String {
        return "${String.format("%.0f", value * 100)}%"
    }

    @JvmStatic
    fun plotPercent(prefix: String, value: Double): String {
        return "$prefix${plotPercent(value)}"
    }

    @JvmStatic
    fun plotPower(prefix: String, value: Double): String {
        return "$prefix${plotValue(value, "W")}"
    }

    @JvmStatic
    fun plotPower(value: Double): String {
        return plotValue(value, "W")
    }

    @JvmStatic
    fun plotUIP(U: Double, I: Double): String {
        return "${plotValue(U, "V")} ${plotValue(I, "A")}"
    }

    @JvmStatic
    fun plotUIP(U: Double, I: Double, R: Double): String {
        return "${plotValue(U, "V")} ${plotValue(I, "A")} ${plotValue(R, "Ω")}"
    }

    @JvmStatic
    fun plotEnergy(prefix: String, value: Double): String {
        return "$prefix${plotValue(value, "J")}"
    }

    @JvmStatic
    fun plotEnergy(value: Double): String {
        return plotValue(value, "J")
    }

    @JvmStatic
    fun plotOhm(prefix: String, value: Double): String {
        return "$prefix${plotValue(value, "Ω")}"
    }

    @JvmStatic
    fun plotOhm(value: Double): String {
        return plotValue(value, "Ω")
    }

    @JvmStatic
    fun plotCelsius(prefix: String, value: Double): String {
        return "$prefix${plotValue(value, "°C")}"
    }

    @JvmStatic
    fun plotCelsius(value: Double): String {
        return plotValue(value, "°C")
    }
    
    @JvmStatic
    fun plotTime(prefix: String, value: Double): String {
        return "$prefix${plotValue(value, "s")}"
    }
    
    @JvmStatic
    fun plotTime(value: Double): String {
        return plotValue(value, "s")
    }

    @JvmStatic
    fun plotBuckets(value: Double): String {
        return "${String.format("%.2f", value / 1000.0)} B"
    }

    @JvmStatic
    fun plotBuckets(prefix: String, value: Double): String {
        return "$prefix${plotBuckets(value)}"
    }

    @JvmStatic
    fun renderDoubleSubsystemWaila(s1: mods.eln.sim.SubSystem?, s2: mods.eln.sim.SubSystem?): String {
        val size1 = s1?.matrix?.size ?: 0
        val size2 = s2?.matrix?.size ?: 0
        return "$size1 / $size2"
    }

    @JvmStatic
    fun renderSubSystemWaila(subSystem: mods.eln.sim.SubSystem?): String {
        val size = subSystem?.matrix?.size ?: 0
        return "$size"
    }

    @JvmStatic
    fun isPlayerAround(level: Level, aabb: AABB): Boolean {
        return level.getEntitiesOfClass(net.minecraft.world.entity.player.Player::class.java, aabb).isNotEmpty()
    }

    @JvmStatic
    fun isPlayerUsingWrench(player: net.minecraft.world.entity.player.Player): Boolean {
        // TODO: Implement proper wrench check
        return false
    }

    @JvmStatic
    fun getSixNodePinDistance(part: Obj3D.Obj3DPart?): Double {
        return 0.0 // Stub
    }

    @JvmStatic
    fun serialiseItemStack(stream: java.io.DataOutputStream, stack: net.minecraft.world.item.ItemStack?) {
        if (stack == null || stack.isEmpty) {
            stream.writeInt(-1)
        } else {
            stream.writeInt(net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(stack.item))
            stream.writeInt(stack.damageValue)
        }
    }

    @JvmStatic
    fun unserialiseItemStack(stream: java.io.DataInputStream): net.minecraft.world.item.ItemStack? {
        val id = stream.readInt()
        if (id == -1) return null
        val damage = stream.readInt()
        val item = net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(id)
        val stack = net.minecraft.world.item.ItemStack(item)
        stack.damageValue = damage
        return stack
    }

    @JvmStatic
    fun getVec05(c: Coordinate): net.minecraft.world.phys.Vec3 {
        return net.minecraft.world.phys.Vec3(c.x + 0.5, c.y + 0.5, c.z + 0.5)
    }

    @JvmStatic
    fun sendPacketToClient(bos: java.io.ByteArrayOutputStream, player: net.minecraft.server.level.ServerPlayer) {
        // TODO: Implement packet sending
    }

    class TraceRayWeightOpaque

    @JvmStatic
    fun traceRay(world: Level, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, weight: TraceRayWeightOpaque): Float {
        // TODO: Implement ray tracing for sound occlusion
        return 0f
    }

    @JvmStatic
    fun getLevel(dimensionId: Int): Level? {
        val server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer() ?: return null
        val key = getLevelKey(dimensionId)
        return server.getLevel(key)
    }

    @JvmStatic
    fun getLevelKey(dimensionId: Int): net.minecraft.resources.ResourceKey<Level> {
        return when(dimensionId) {
            0 -> Level.OVERWORLD
            -1 -> Level.NETHER
            1 -> Level.END
            else -> net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation("eln", "dim_$dimensionId"))
        }
    }

    @JvmStatic
    fun getDimensionId(level: Level): Int {
        val key = level.dimension()
        return when(key) {
            Level.OVERWORLD -> 0
            Level.NETHER -> -1
            Level.END -> 1
            else -> {
                // Try to parse "dim_X"
                val path = key.location().path
                if (path.startsWith("dim_")) {
                    path.substring(4).toIntOrNull() ?: 0
                } else {
                    0 // Fallback
                }
            }
        }
    }

    @JvmStatic
    fun printFunction(function: FunctionTable, min: Double, max: Double, step: Double) {
        // Stub
    }

    const val coalEnergyReference = 16000.0

    @JvmStatic
    fun entityLivingViewDirection(entity: LivingEntity): Direction {
        return Direction.fromMCDirection(net.minecraft.core.Direction.orderedByNearest(entity)[0])
    }

    @JvmStatic
    fun entityLivingHorizontalViewDirection(entity: LivingEntity): Direction {
        return Direction.fromMCDirection(entity.direction)
    }
}

fun MutableList<net.minecraft.network.chat.Component>.add(text: String): Boolean {
    return this.add(net.minecraft.network.chat.Component.literal(text))
}

fun net.minecraft.world.item.Item.newItemStack(count: Int = 1): net.minecraft.world.item.ItemStack {
        return net.minecraft.world.item.ItemStack(this, count)
    }

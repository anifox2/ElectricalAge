package mods.eln.misc

import mods.eln.Eln
import mods.eln.ElnNetwork
import mods.eln.ElnPacket
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import java.io.DataInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.phys.AABB
import net.minecraft.world.level.Level
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.lwjgl.opengl.GL11

object Utils {
    @JvmStatic
    fun println(message: String, vararg args: Any?) {
        Eln.LOGGER.info(String.format(message, *args))
    }

    @JvmStatic
    fun println(message: Any?) {
        Eln.LOGGER.info(message.toString())
    }

    @JvmStatic
    fun rand(min: Double, max: Double): Double {
        return min + Math.random() * (max - min)
    }

    @JvmStatic
    var uuid = 0
        get() = field++
        private set


    @JvmStatic
    fun plotVolt(value: Double): String {
        return plotValue(value, "V  ")
    }

    @JvmStatic
    fun plotVolt(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotVolt(value)
    }


    @JvmStatic
    fun plotAmpere(value: Double): String {
        return plotValue(value, "A  ")
    }

    @JvmStatic
    fun plotAmpere(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotAmpere(value)
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
    fun getOrCreateCompound(nbt: CompoundTag, key: String): CompoundTag {
        if (!nbt.contains(key, 10)) { // 10 is TAG_COMPOUND
            nbt.put(key, CompoundTag())
        }
        return nbt.getCompound(key)
    }

    @JvmStatic
    fun canPutStackInInventory(stacks: Array<ItemStack>, inventory: net.minecraft.world.Container, slots: IntArray): Boolean {
        val copyInventory = java.util.ArrayList<ItemStack>()
        for (slot in slots) {
            copyInventory.add(inventory.getItem(slot).copy())
        }

        for (stackToAdd in stacks) {
            if (stackToAdd.isEmpty) continue
            var remaining = stackToAdd.count
            for (i in slots.indices) {
                val slotStack = copyInventory[i]
                
                if (slotStack.isEmpty) {
                    val toAdd = Math.min(remaining, Math.min(inventory.maxStackSize, stackToAdd.maxStackSize))
                    val newStack = stackToAdd.copy()
                    newStack.count = toAdd
                    copyInventory[i] = newStack
                    remaining -= toAdd
                } else if (ItemStack.isSameItemSameTags(slotStack, stackToAdd)) {
                    val space = Math.min(inventory.maxStackSize, slotStack.maxStackSize) - slotStack.count
                    val toAdd = Math.min(remaining, space)
                    if (toAdd > 0) {
                        slotStack.grow(toAdd)
                        remaining -= toAdd
                    }
                }
                if (remaining <= 0) break
            }
            if (remaining > 0) return false
        }
        return true
    }

    @JvmStatic
    fun tryPutStackInInventory(stacks: Array<ItemStack>, inventory: net.minecraft.world.Container, slots: IntArray): Boolean {
        if (!canPutStackInInventory(stacks, inventory, slots)) return false
        
        for (stackToAdd in stacks) {
            if (stackToAdd.isEmpty) continue
            var remaining = stackToAdd.count
            for (slotIndex in slots) {
                val slotStack = inventory.getItem(slotIndex)
                
                if (slotStack.isEmpty) {
                    val toAdd = Math.min(remaining, Math.min(inventory.maxStackSize, stackToAdd.maxStackSize))
                    val newStack = stackToAdd.copy()
                    newStack.count = toAdd
                    inventory.setItem(slotIndex, newStack)
                    remaining -= toAdd
                } else if (ItemStack.isSameItemSameTags(slotStack, stackToAdd)) {
                    val space = Math.min(inventory.maxStackSize, slotStack.maxStackSize) - slotStack.count
                    val toAdd = Math.min(remaining, space)
                    if (toAdd > 0) {
                        slotStack.grow(toAdd)
                        remaining -= toAdd
                    }
                }
                if (remaining <= 0) break
            }
        }
        return true
    }

    @JvmStatic
    fun load(nbt: net.minecraft.nbt.CompoundTag, key: String, obj: Any?) {
        if (obj is INBTTReady) {
            obj.readFromNBT(nbt, key)
        }
    }

    @JvmStatic
    fun save(nbt: net.minecraft.nbt.CompoundTag, key: String, obj: Any?) {
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
    fun plotValue(value: Double, unit: String): String {
        if (Math.abs(value) < 1e-9) return "0.00 $unit"
        if (Math.abs(value) < 1e-6) return String.format("%.2fn%s", value * 1e9, unit)
        if (Math.abs(value) < 1e-3) return String.format("%.2fu%s", value * 1e6, unit)
        if (Math.abs(value) < 1.0) return String.format("%.2fm%s", value * 1e3, unit)
        if (Math.abs(value) < 1e3) return String.format("%.2f %s", value, unit)
        if (Math.abs(value) < 1e6) return String.format("%.2fk%s", value / 1e3, unit)
        if (Math.abs(value) < 1e9) return String.format("%.2fM%s", value / 1e6, unit)
        return String.format("%.2fG%s", value / 1e9, unit)
    }
    
    @JvmStatic
    fun plotValue(value: Double): String {
        return plotValue(value, "")
    }



    @JvmStatic
    fun modbusToShort(v: Float, part: Int): Short {
        val i = java.lang.Float.floatToIntBits(v)
        return if (part == 0) (i and 0xFFFF).toShort() else ((i ushr 16) and 0xFFFF).toShort()
    }

    @JvmStatic
    fun modbusToFloat(low: Short, high: Short): Float {
        val i = (low.toInt() and 0xFFFF) or ((high.toInt() and 0xFFFF) shl 16)
        return java.lang.Float.intBitsToFloat(i)
    }
    


    @JvmStatic
    fun plotCelsius(header: String, value: Double): String {
        var header = header
        var value = value
        value += mods.eln.sim.PhysicalConstant.ambientTemperatureKelvin - mods.eln.sim.PhysicalConstant.zeroCelsiusInKelvin
        if (header != "") header += " "
        return header + plotValue(value, "\u00B0C ")
    }

    @JvmStatic
    fun plotPercent(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return if (value >= 1.0) header + String.format("%3.0f", value * 100.0) + "%   " else header + String.format("%3.1f", value * 100.0) + "%   "
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
    fun plotUIPDetailed(voltage: Double, current: Double): String {
        return "U: ${plotVolt(voltage)} I: ${plotAmpere(current)} P: ${plotValue(voltage * current, "W")}"
    }

    @JvmStatic
    fun plotUIP(U: Double, I: Double, R: Double): String {
        return "${plotValue(U, "V")} ${plotValue(I, "A")} ${plotValue(R, "Ω")}"
    }

    @JvmStatic
    fun plotEnergy(value: Double): String {
        return plotValue(value, "J  ")
    }

    @JvmStatic
    fun plotEnergy(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotEnergy(value)
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
    fun plotRads(header: String, value: Double): String {
        var header = header
        if (header != "") header += " "
        return header + plotValue(value, "rad/s ")
    }

    @JvmStatic
    fun plotER(E: Double, R: Double): String {
        return plotEnergy("E", E) + plotRads("R", R)
    }
    
    @JvmStatic
    fun plotER(header: String, load: mods.eln.sim.ElectricalLoad): String {
        return header + " " + plotVolt("U", load.getVoltage()) + " " + plotOhm("R", load.getSerialResistance())
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
    fun renderDoubleSubsystemWaila(s1: mods.eln.sim.mna.SubSystem?, s2: mods.eln.sim.mna.SubSystem?): String {
        val size1 = s1?.states?.size ?: 0
        val size2 = s2?.states?.size ?: 0
        return "$size1 / $size2"
    }

    @JvmStatic
    fun renderSubSystemWaila(subSystem: mods.eln.sim.mna.SubSystem?): String {
        val size = subSystem?.states?.size ?: 0
        return "$size"
    }

    @JvmStatic
    fun isPlayerAround(level: Level, aabb: AABB): Boolean {
        return level.getEntitiesOfClass(net.minecraft.world.entity.player.Player::class.java, aabb).isNotEmpty()
    }

    @JvmStatic
    fun isPlayerUsingWrench(player: net.minecraft.world.entity.player.Player): Boolean {
        val stack = player.mainHandItem
        if (stack.isEmpty) return false
        val wrench = Eln.wrenchItemStack
        return wrench != null && stack.item == wrench.item && stack.elnMetadata == wrench.elnMetadata
    }

    @JvmStatic
    fun getSixNodePinDistance(part: Obj3D.Obj3DPart?): FloatArray {
        return FloatArray(6) { 0f } // Stub
    }

    @JvmStatic
    fun serialiseItemStack(stream: java.io.DataOutputStream, stack: net.minecraft.world.item.ItemStack?) {
        if (stack == null || stack.isEmpty) {
            stream.writeInt(-1)
        } else {
            stream.writeInt(net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(stack.item))
            stream.writeInt(stack.elnMetadata)
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
        stack.elnMetadata = damage
        return stack
    }

    @JvmStatic
    fun unserializeItemStackToItemEntity(stream: java.io.DataInputStream, old: net.minecraft.world.entity.item.ItemEntity?, tileEntity: net.minecraft.world.level.block.entity.BlockEntity): net.minecraft.world.entity.item.ItemEntity? {
        val stack = unserialiseItemStack(stream)
        if (stack == null) {
            old?.discard()
            return null
        }
        if (old != null && old.isAlive) {
            old.item = stack
            return old
        }
        val pos = tileEntity.blockPos
        val entity = net.minecraft.world.entity.item.ItemEntity(tileEntity.level!!, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, stack)
        entity.deltaMovement = net.minecraft.world.phys.Vec3.ZERO
        entity.setPickUpDelay(40)
        return entity
    }

    @JvmStatic
    fun getVec05(c: Coordinate): net.minecraft.world.phys.Vec3 {
        return net.minecraft.world.phys.Vec3(c.x + 0.5, c.y + 0.5, c.z + 0.5)
    }

    @JvmStatic
    fun sendPacketToClient(bos: java.io.ByteArrayOutputStream, player: net.minecraft.server.level.ServerPlayer) {
        ElnNetwork.sendToClient(ElnPacket(bos.toByteArray()), player)
    }

    class TraceRayWeightOpaque

    @JvmStatic
    fun traceRay(world: Level, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double, weight: TraceRayWeightOpaque): Float {
        val start = Vec3(x1, y1, z1)
        val end = Vec3(x2, y2, z2)
        val context = net.minecraft.world.level.ClipContext(start, end, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, null)
        val result = world.clip(context)
        return if (result.type == net.minecraft.world.phys.HitResult.Type.MISS) 1.0f else 0.0f
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
        return when (level.dimension()) {
            Level.OVERWORLD -> 0
            Level.NETHER -> -1
            Level.END -> 1
            else -> 0
        }
    }

    @JvmStatic
    fun printFunction(function: FunctionTable, min: Double, max: Double, step: Double) {
        // Stub
    }

    const val coalEnergyReference = 16000.0

    @JvmStatic
    fun entityLivingViewDirection(entity: LivingEntity): Direction {
        return Direction.fromIntMinecraftSide(net.minecraft.core.Direction.orderedByNearest(entity)[0].ordinal)
    }

    @JvmStatic
    fun entityLivingHorizontalViewDirection(entity: LivingEntity): Direction {
        return Direction.fromMCDirection(entity.direction)
    }

    @JvmStatic
    fun mustDropItem(player: net.minecraft.server.level.ServerPlayer?): Boolean {
        return player == null || !player.isCreative
    }

    @JvmStatic
    val minecraftDay = 24000.0

    @JvmStatic
    fun newNbtTagCompund(nbt: CompoundTag?, string: String): CompoundTag {
        val tag = CompoundTag()
        nbt?.put(string, tag)
        return tag
    }

    @JvmStatic
    fun updateAllLightTypes(level: Level, pos: net.minecraft.core.BlockPos) {
        // Stub
    }

    @JvmStatic
    fun updateSkylight(level: Level, pos: net.minecraft.core.BlockPos) {
        // Stub
    }

    @JvmStatic
    fun fatal(message: String) {
        Eln.LOGGER.error(message)
        throw RuntimeException(message)
    }

    @JvmStatic
    fun isRemote(level: Level): Boolean {
        return level.isClientSide
    }

    @JvmStatic
    fun notifyNeighbor(level: Level, x: Int, y: Int, z: Int) {
        val pos = net.minecraft.core.BlockPos(x, y, z)
        val state = level.getBlockState(pos)
        level.updateNeighborsAt(pos, state.block)
    }

    @JvmStatic
    fun getItemObject(stack: ItemStack): Any? {
        return mods.eln.generic.GenericItemUsingDamageDescriptor.getDescriptor(stack)
    }

    @JvmStatic
    fun setGlColorFromDye(damage: Int) {
        setGlColorFromDye(damage, 1.0f)
    }

    @JvmStatic
    fun setGlColorFromDye(damage: Int, gain: Float) {
        setGlColorFromDye(damage, gain, 0f)
    }

    @JvmStatic
    fun setGlColorFromDye(damage: Int, gain: Float, bias: Float) {
        when (damage) {
            0 -> GL11.glColor3f(0.2f * gain + bias, 0.2f * gain + bias, 0.2f * gain + bias)
            1 -> GL11.glColor3f(1.0f * gain + bias, 0.05f * gain + bias, 0.05f * gain + bias)
            2 -> GL11.glColor3f(0.2f * gain + bias, 0.5f * gain + bias, 0.1f * gain + bias)
            3 -> GL11.glColor3f(0.3f * gain + bias, 0.2f * gain + bias, 0.1f * gain + bias)
            4 -> GL11.glColor3f(0.2f * gain + bias, 0.2f * gain + bias, 1.0f * gain + bias)
            5 -> GL11.glColor3f(0.7f * gain + bias, 0.05f * gain + bias, 1.0f * gain + bias)
            6 -> GL11.glColor3f(0.2f * gain + bias, 0.7f * gain + bias, 0.9f * gain + bias)
            7 -> GL11.glColor3f(0.7f * gain + bias, 0.7f * gain + bias, 0.7f * gain + bias)
            8 -> GL11.glColor3f(0.4f * gain + bias, 0.4f * gain + bias, 0.4f * gain + bias)
            9 -> GL11.glColor3f(1.0f * gain + bias, 0.5f * gain + bias, 0.5f * gain + bias)
            10 -> GL11.glColor3f(0.05f * gain + bias, 1.0f * gain + bias, 0.05f * gain + bias)
            11 -> GL11.glColor3f(0.9f * gain + bias, 0.8f * gain + bias, 0.1f * gain + bias)
            12 -> GL11.glColor3f(0.4f * gain + bias, 0.5f * gain + bias, 1.0f * gain + bias)
            13 -> GL11.glColor3f(0.9f * gain + bias, 0.3f * gain + bias, 0.9f * gain + bias)
            14 -> GL11.glColor3f(1.0f * gain + bias, 0.6f * gain + bias, 0.3f * gain + bias)
            15 -> GL11.glColor3f(1.0f * gain + bias, 1.0f * gain + bias, 1.0f * gain + bias)
            else -> GL11.glColor3f(0.05f * gain + bias, 0.05f * gain + bias, 0.05f * gain + bias)
        }
    }

    @JvmStatic
    fun getWind(dimension: Int, y: Int): Double {
        return 0.0 // Stub
    }

    @JvmStatic
    fun getSixNodePinDistance(direction: Direction): FloatArray {
        return floatArrayOf(0f, 0f) // Stub
    }

    @JvmStatic
    fun getLength(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
        val dx = x1 - x2
        val dy = y1 - y2
        val dz = z1 - z2
        return Math.sqrt(dx * dx + dy * dy + dz * dz)
    }

    @JvmStatic
    fun playerHasMeter(player: net.minecraft.world.entity.player.Player): Boolean {
        return false // Stub
    }

    @JvmStatic
    fun dropItem(stack: ItemStack, x: Int, y: Int, z: Int, level: Level) {
        if (stack.isEmpty) return
        val f = 0.7
        val d0 = (level.random.nextFloat() * f).toDouble() + (1.0 - f) * 0.5
        val d1 = (level.random.nextFloat() * f).toDouble() + (1.0 - f) * 0.5
        val d2 = (level.random.nextFloat() * f).toDouble() + (1.0 - f) * 0.5
        val entityitem = net.minecraft.world.entity.item.ItemEntity(level, x.toDouble() + d0, y.toDouble() + d1, z.toDouble() + d2, stack)
        entityitem.setDefaultPickUpDelay()
        level.addFreshEntity(entityitem)
    }

    @JvmStatic
    fun setGlColorFromLamp(color: Int) {
        // Stub
    }

    @JvmStatic
    fun readMapFile(path: String): String {
        val sb = StringBuilder()
        try {
            val stream = Utils::class.java.getResourceAsStream("/assets/eln/map/$path")
            if (stream != null) {
                val reader = BufferedReader(InputStreamReader(stream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                reader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    @JvmStatic
    fun getRedstoneLevelAround(coord: Coordinate, side: Direction): Int {
        return 0
    }

    @JvmStatic
    fun unserializeItemStack(stream: DataInputStream): ItemStack {
        return unserialiseItemStack(stream) ?: ItemStack.EMPTY
    }
}

fun MutableList<net.minecraft.network.chat.Component>.add(text: String): Boolean {
    return this.add(net.minecraft.network.chat.Component.literal(text))
}

fun net.minecraft.world.item.Item.newItemStack(count: Int = 1): net.minecraft.world.item.ItemStack {
        return net.minecraft.world.item.ItemStack(this, count)
    }

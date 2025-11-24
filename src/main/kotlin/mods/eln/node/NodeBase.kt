@file:Suppress("NAME_SHADOWING")
package mods.eln.node

import mods.eln.misc.Utils.println
import mods.eln.misc.Utils.addChatMessage
import mods.eln.misc.Coordinate
import net.minecraft.server.level.ServerPlayer
import mods.eln.misc.LRDUCubeMask
import net.minecraft.world.level.Level
import mods.eln.Eln
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import mods.eln.sound.SoundCommand
// import mods.eln.GuiHandler
import mods.eln.misc.LRDU
import mods.eln.sim.ThermalLoad
import mods.eln.sim.ElectricalLoad
import mods.eln.node.six.SixNode
import mods.eln.sim.IProcess
import mods.eln.misc.INBTTReady
import java.io.IOException
import kotlin.jvm.JvmOverloads
import net.minecraft.server.MinecraftServer
import mods.eln.ServerKeyHandler
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.Container
import net.minecraftforge.server.ServerLifecycleHooks
import net.minecraft.world.level.block.Blocks
import mods.eln.ghost.GhostBlock
import mods.eln.misc.Direction
import mods.eln.misc.Utils
import mods.eln.sim.ElectricalConnection
import mods.eln.sim.ThermalConnection
import net.minecraft.world.level.block.Block
import net.minecraft.world.entity.Entity
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.ArrayList
import kotlin.experimental.or

abstract class NodeBase {
    var neighborOpaque: Byte = 0
    var neighborWrapable: Byte = 0
    @JvmField
    var coordinate: Coordinate
    var level: Level? = null
    @JvmField
    var nodeConnectionList = ArrayList<NodeConnection>(4)
    private var initialized = false
    private var isAdded = false
    var needPublish = false

    // public static boolean canBePlacedOn(ItemStack itemStack,Direction side)
    open fun mustBeSaved(): Boolean {
        return true
    }

    open val blockMetadata: Int
        get() = 0

    open fun networkUnserialize(stream: DataInputStream, player: ServerPlayer?) {}
    fun notifyNeighbor() {
        val world = level ?: return
        val pos = coordinate.toBlockPos()
        val state = world.getBlockState(pos)
        world.sendBlockUpdated(pos, state, state, 3)
    }

    //public abstract Block getBlock();
    abstract val nodeUuid: String?
    @JvmField
    var lrduCubeMask = LRDUCubeMask()
    fun neighborBlockRead() {
        val world = level ?: return
        neighborOpaque = 0
        neighborWrapable = 0
        for (direction in Direction.values()) {
            val pos = coordinate.toBlockPos().offset(direction.toMCDirection().normal)
            val state = world.getBlockState(pos)
            
            neighborOpaque = neighborOpaque or (1 shl direction.int).toByte()
            if (isBlockWrappable(state, world, pos)) neighborWrapable = neighborWrapable or (1 shl direction.int).toByte()
        }
    }

    open fun hasGui(side: Direction): Boolean {
        return false
    }

    open fun onNeighborBlockChange() {
        neighborBlockRead()
        if (isAdded) {
            reconnect()
        }
    }

    fun isBlockWrappable(direction: Direction): Boolean {
        return neighborWrapable.toInt() shr direction.int and 1 != 0
    }

    fun isBlockOpaque(direction: Direction): Boolean {
        return neighborOpaque.toInt() shr direction.int and 1 != 0
    }

    var isDestructing = false
    fun physicalSelfDestruction(explosionStrength: Float) {
        var explosionStrength = explosionStrength
        if (isDestructing) return
        isDestructing = true
        if (!Eln.explosionEnable) explosionStrength = 0f
        disconnect()
        val pos = coordinate.toBlockPos()
        val world = level ?: return
        world.removeBlock(pos, false)
        NodeManager.instance!!.removeNode(this)
        if (explosionStrength != 0f) {
            world.explode(null, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), explosionStrength, Level.ExplosionInteraction.BLOCK)
        }
    }

    fun onBlockPlacedBy(level: Level, coordinate: Coordinate, front: Direction, entityLiving: LivingEntity?, itemStack: ItemStack?) {
        this.level = level
        this.coordinate = coordinate
        neighborBlockRead()
        NodeManager.instance!!.addNode(this)
        initializeFromThat(front, entityLiving, itemStack)
        if (itemStack != null) println("Node::constructor( meta = " + itemStack.damageValue + ")")
    }

    abstract fun initializeFromThat(front: Direction, entityLiving: LivingEntity?, itemStack: ItemStack?)

    fun getNeighbor(direction: Direction): NodeBase? {
        val pos = coordinate.toBlockPos().offset(direction.toMCDirection().normal)
        val nodeCoordinate = Coordinate(pos, coordinate.dimension)
        return NodeManager.instance!!.getNodeFromCoordonate(nodeCoordinate)
    }

    open fun onBreakBlock() {
        isDestructing = true
        disconnect()
        NodeManager.instance!!.removeNode(this)
        println("Node::onBreakBlock()")
    }

    open fun onChunkUnload() {
        disconnect()
        NodeManager.instance!!.removeNode(this)
    }

    open fun onBlockActivated(entityPlayer: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        if (!entityPlayer.level().isClientSide) {
            val equipped = entityPlayer.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND)
            if (!equipped.isEmpty) {
                if (Eln.multiMeterElement?.checkSameItemStack(equipped) == true) {
                    val str = multiMeterString(side)
                    addChatMessage(entityPlayer, str)
                    return true
                }
                if (Eln.thermometerElement?.checkSameItemStack(equipped) == true) {
                    val str = thermoMeterString(side)
                    addChatMessage(entityPlayer, str)
                    return true
                }
                if (Eln.allMeterElement?.checkSameItemStack(equipped) == true) {
                    val str1 = multiMeterString(side)
                    val str2 = thermoMeterString(side)
                    addChatMessage(entityPlayer, "$str1 $str2")
                    return true
                }
                if (Eln.portableOreScannerElement?.checkSameItemStack(equipped) == true) {
                    val str = multiMeterString(side)
                    addChatMessage(entityPlayer, str)
                    return true
                }
            }
        }
        return false
    }

    fun reconnect() {
        disconnect()
        connect()
    }

    abstract fun getSideConnectionMask(side: Direction, lrdu: LRDU): Int
    abstract fun getThermalLoad(side: Direction, lrdu: LRDU, mask: Int): ThermalLoad?
    abstract fun getElectricalLoad(side: Direction, lrdu: LRDU, mask: Int): ElectricalLoad?
    open fun checkCanStay(onCreate: Boolean) {}
    open fun connectJob() {
        // EXTERNAL OTHERS SIXNODE
        run {
            for (direction in Direction.values()) {
                if (isBlockWrappable(direction)) {
                    val emptyBlockPos = coordinate.toBlockPos().offset(direction.toMCDirection().normal)
                    for (lrdu in LRDU.values()) {
                        val elementSide = direction.applyLRDU(lrdu)
                        val otherBlockPos = emptyBlockPos.offset(elementSide.toMCDirection().normal)
                        val otherNode = NodeManager.instance?.getNodeFromCoordonate(Coordinate(otherBlockPos, coordinate.dimension))
                            ?: continue
                        val otherDirection = elementSide.inverse()
                        val otherLRDU = otherDirection.getLRDUGoingTo(direction)!!.inverse()
                        if (this is SixNode || otherNode is SixNode) {
                            tryConnectTwoNode(this, direction, lrdu, otherNode, otherDirection, otherLRDU)
                        }
                    }
                }
            }
        }
        run {
            for (dir in Direction.values()) {
                val otherNode = getNeighbor(dir)
                if (otherNode != null && otherNode.isAdded) {
                    for (lrdu in LRDU.values()) {
                        tryConnectTwoNode(this, dir, lrdu, otherNode, dir.inverse(), lrdu.inverseIfLR())
                    }
                }
            }
        }
    }

    open fun disconnectJob() {
        for (c in nodeConnectionList) {
            if (c.N1 !== this) {
                c.N1.nodeConnectionList.remove(c)
                c.N1.needPublish = true
                c.N1.lrduCubeMask[c.dir1, c.lrdu1] = false
            }
            if (c.N2 !== this) {
                c.N2.nodeConnectionList.remove(c)
                c.N2.needPublish = true
                c.N2.lrduCubeMask[c.dir2, c.lrdu2] = false
            }
            c.destroy()
        }
        lrduCubeMask.clear()
        nodeConnectionList.clear()
    }

    open fun externalDisconnect(side: Direction?, lrdu: LRDU?) {}
    open fun newConnectionAt(connection: NodeConnection?, isA: Boolean) {}
    open fun connectInit() {
        lrduCubeMask.clear()
        nodeConnectionList.clear()
    }

    fun connect() {
        if (isAdded) {
            disconnect()
        }
        connectInit()
        connectJob()
        isAdded = true
        needPublish = true
    }

    fun disconnect() {
        if (!isAdded) {
            println("Node destroy error already destroy")
            return
        }
        disconnectJob()
        isAdded = false
    }

    open fun nodeAutoSave(): Boolean {
        return true
    }

    open fun readFromNBT(nbt: CompoundTag) {
        coordinate.readFromNBT(nbt, "c")
        neighborOpaque = nbt.getByte("NBOpaque")
        neighborWrapable = nbt.getByte("NBWrap")
        initialized = true
    }

    open fun writeToNBT(nbt: CompoundTag) {
        coordinate.writeToNBT(nbt, "c")
        nbt.putByte("NBOpaque", neighborOpaque)
        nbt.putByte("NBWrap", neighborWrapable)
    }

    open fun multiMeterString(side: Direction): String {
        return ""
    }

    open fun thermoMeterString(side: Direction): String {
        return ""
    }

    open fun readConfigTool(side: Direction?, tag: CompoundTag?, invoker: Player?): Boolean {
        return false
    }

    open fun writeConfigTool(side: Direction?, tag: CompoundTag?, invoker: Player?): Boolean {
        return false
    }

    private fun isINodeProcess(process: IProcess): Boolean {
        for (c in process.javaClass.interfaces) {
            if (c == INBTTReady::class.java) return true
        }
        return false
    }

    @JvmField
    var needNotify = false
    open fun publishSerialize(stream: DataOutputStream) {}
    fun preparePacketForClient(stream: DataOutputStream) {
        try {
            stream.writeByte(Eln.packetForClientNode.toInt())
            stream.writeInt(coordinate.x)
            stream.writeInt(coordinate.y)
            stream.writeInt(coordinate.z)
            stream.writeByte(coordinate.dimension)
            stream.writeUTF(nodeUuid!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendPacketToClient(bos: ByteArrayOutputStream?, player: ServerPlayer?) {
        Utils.sendPacketToClient(bos!!, player!!)
    }

    @JvmOverloads
    fun sendPacketToAllClient(bos: ByteArrayOutputStream?, range: Double = 100000.0) {
        val server = ServerLifecycleHooks.getCurrentServer()
        for (player in server.playerList.players) {
            val playerDim = when (player.level().dimension()) {
                net.minecraft.world.level.Level.OVERWORLD -> 0
                net.minecraft.world.level.Level.NETHER -> -1
                net.minecraft.world.level.Level.END -> 1
                else -> 0
            }
            if (playerDim != coordinate.dimension) continue
            if (coordinate.distanceTo(player) > range) continue
            Utils.sendPacketToClient(bos!!, player)
        }
    }

    val publishPacket: ByteArrayOutputStream?
        get() {
            val bos = ByteArrayOutputStream(64)
            val stream = DataOutputStream(bos)
            try {
                stream.writeByte(Eln.packetNodeSingleSerialized.toInt())
                stream.writeInt(coordinate.x)
                stream.writeInt(coordinate.y)
                stream.writeInt(coordinate.z)
                stream.writeByte(coordinate.dimension)
                stream.writeUTF(nodeUuid!!)
                publishSerialize(stream)
                return bos
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

    fun publishToAllPlayer() {
        val server = ServerLifecycleHooks.getCurrentServer()
        for (player in server.playerList.players) {
            val playerDim = when (player.level().dimension()) {
                net.minecraft.world.level.Level.OVERWORLD -> 0
                net.minecraft.world.level.Level.NETHER -> -1
                net.minecraft.world.level.Level.END -> 1
                else -> 0
            }
            if (playerDim != coordinate.dimension) continue
            if (coordinate.distanceTo(player) > 192.0) continue // Approx view distance
            Utils.sendPacketToClient(publishPacket!!, player)
        }
        if (needNotify) {
            needNotify = false
            notifyNeighbor()
        }
        needPublish = false
    }

    fun publishToPlayer(player: ServerPlayer?) {
        Utils.sendPacketToClient(publishPacket!!, player!!)
    }

    fun dropItem(itemStack: ItemStack?) {
        if (itemStack == null) return
        val world = level ?: ServerLifecycleHooks.getCurrentServer().getLevel(
             when (coordinate.dimension) {
                0 -> net.minecraft.world.level.Level.OVERWORLD
                -1 -> net.minecraft.world.level.Level.NETHER
                1 -> net.minecraft.world.level.Level.END
                else -> net.minecraft.world.level.Level.OVERWORLD
            }
        ) ?: return

        if (world.gameRules.getBoolean(net.minecraft.world.level.GameRules.RULE_DOBLOCKDROPS)) {
            val var6 = 0.7f
            val var7 = (world.random.nextFloat() * var6).toDouble() + (1.0f - var6).toDouble() * 0.5
            val var9 = (world.random.nextFloat() * var6).toDouble() + (1.0f - var6).toDouble() * 0.5
            val var11 = (world.random.nextFloat() * var6).toDouble() + (1.0f - var6).toDouble() * 0.5
            val var13 = ItemEntity(world, coordinate.x.toDouble() + var7, coordinate.y.toDouble() + var9, coordinate.z.toDouble() + var11, itemStack)
            var13.setPickUpDelay(10)
            world.addFreshEntity(var13)
        }
    }

    fun dropInventory(inventory: Container?) {
        if (inventory == null) return
        for (idx in 0 until inventory.containerSize) {
            dropItem(inventory.getItem(idx))
        }
    }

    abstract fun initializeFromNBT()
    open fun globalBoot() {}
    fun needPublish() {
        needPublish = true
    }

    open fun unload() {
        disconnect()
    }

    companion object {
        const val maskElectricalPower = 1 shl 0
        const val maskThermal = 1 shl 1
        const val maskElectricalGate = 1 shl 2
        const val maskElectricalAll = maskElectricalPower or maskElectricalGate
        const val maskElectricalInputGate = maskElectricalGate
        const val maskElectricalOutputGate = maskElectricalGate
        const val maskWire = 0
        const val maskElectricalWire = 1 shl 3
        const val maskThermalWire = maskWire + maskThermal
        const val maskSignal = 1 shl 9
        const val maskRs485 = 1 shl 10
        const val maskSignalBus = 1 shl 11
        const val maskConduit = 1 shl 12
        const val maskColorData = 0xF shl 16
        const val maskColorShift = 16
        const val maskColorCareShift = 20
        const val maskColorCareData = 1 shl 20
        const val networkSerializeUFactor = 10.0
        const val networkSerializeIFactor = 100.0
        const val networkSerializeTFactor = 10.0
        var teststatic = 0
        @JvmStatic
        fun isBlockWrappable(state: net.minecraft.world.level.block.state.BlockState, w: Level, pos: net.minecraft.core.BlockPos): Boolean {
            val block = state.block
            if (state.canBeReplaced()) return true
            if (block === Blocks.AIR) return true
            if (block === Eln.sixNodeBlock) return true
            if (block is GhostBlock) return true
            if (block === Blocks.TORCH) return true
            if (block === Blocks.REDSTONE_TORCH) return true
            //if (block === Blocks.unlit_redstone_torch) return true // Removed in modern versions
            return block === Blocks.REDSTONE_WIRE
        }

        var beepUploaded = SoundCommand("eln:beep_accept_2").smallRange()!!
        var beepDownloaded = SoundCommand("eln:beep_accept").smallRange()!!
        var beepError = SoundCommand("eln:beep_error").smallRange()!!

        fun tryConnectTwoNode(nodeA: NodeBase, directionA: Direction, lrduA: LRDU, nodeB: NodeBase, directionB: Direction, lrduB: LRDU) {
            val mskA = nodeA.getSideConnectionMask(directionA, lrduA)
            val mskB = nodeB.getSideConnectionMask(directionB, lrduB)
            if (compareConnectionMask(mskA, mskB)) {
                val eCon: ElectricalConnection?
                val tCon: ThermalConnection?
                val nodeConnection = NodeConnection(nodeA, directionA, lrduA, nodeB, directionB, lrduB)
                nodeA.nodeConnectionList.add(nodeConnection)
                nodeB.nodeConnectionList.add(nodeConnection)
                nodeA.needPublish = true
                nodeB.needPublish = true
                nodeA.lrduCubeMask[directionA, lrduA] = true
                nodeB.lrduCubeMask[directionB, lrduB] = true
                nodeA.newConnectionAt(nodeConnection, true)
                nodeB.newConnectionAt(nodeConnection, false)
                var eLoad: ElectricalLoad?
                if (nodeA.getElectricalLoad(directionA, lrduA, mskB).also { eLoad = it } != null) {
                    val otherELoad = nodeB.getElectricalLoad(directionB, lrduB, mskA)
                    if (otherELoad != null) {
                        eCon = ElectricalConnection(eLoad, otherELoad)
                        Eln.simulator.addElectricalComponent(eCon)
                        nodeConnection.addConnection(eCon)
                    }
                }
                var tLoad: ThermalLoad?
                if (nodeA.getThermalLoad(directionA, lrduA, mskB).also { tLoad = it } != null) {
                    val otherTLoad = nodeB.getThermalLoad(directionB, lrduB, mskA)
                    if (otherTLoad != null) {
                        tCon = ThermalConnection(tLoad, otherTLoad)
                        Eln.simulator.addThermalConnection(tCon)
                        nodeConnection.addConnection(tCon)
                    }
                }
            }
        }

        @JvmStatic
        fun compareConnectionMask(mask1: Int, mask2: Int): Boolean {
            if (mask1 and 0xFFFF and (mask2 and 0xFFFF) == 0) return false
            if (mask1 and maskColorCareData and (mask2 and maskColorCareData) == 0) return true
            return mask1 and maskColorData == mask2 and maskColorData
        }
    }

    init {
        coordinate = Coordinate()
    }
}

package mods.eln.node.transparent

import mods.eln.misc.elnMetadata
import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.FakeSideInventory.Companion.instance
import mods.eln.misc.FakeFluidHandler
import mods.eln.misc.LRDU
import mods.eln.node.NodeBlockEntity
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB
import net.minecraft.world.level.Level
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraft.nbt.CompoundTag

import mods.eln.init.Registration
import mods.eln.node.NodeManager

open class TransparentNodeBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : NodeBlockEntity(type, pos, state), WorldlyContainer, IFluidHandler {
    constructor(pos: BlockPos, state: BlockState) : this(Registration.TRANSPARENT_NODE_BLOCK_ENTITY.get(), pos, state)
    var elementRender: TransparentNodeElementRender? = null
    var elementRenderId: Short = 0

    override fun onBlockPlacedBy(front: Direction?, entityLiving: net.minecraft.world.entity.LivingEntity?, stack: ItemStack) {
        if (level!!.isClientSide) return
        
        val metadata = stack.elnMetadata
        val node = TransparentNode()
        node.elementId = metadata
        node.coordinate = Coordinate(this)
        node.level = level
        
        try {
            val descriptor = Eln.transparentNodeItem.getDescriptor(metadata)
            if (descriptor != null) {
                 node.element = descriptor.ElementClass.getConstructor(TransparentNode::class.java, TransparentNodeDescriptor::class.java).newInstance(node, descriptor) as TransparentNodeElement
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        NodeManager.instance?.addNode(node)
        this.internalNode = node
        
        node.onBlockPlacedBy(level!!, Coordinate(this), front ?: Direction.N, entityLiving, stack)
        // node.onBlockAdded()
    }

    private val fluidHandler: IFluidHandler
        get() {
            if (level != null && !level!!.isClientSide) {
                val node = node
                if (node != null && node is TransparentNode) {
                    val i = node.fluidHandler
                    if (i != null) {
                        return i
                    }
                }
            }
            return FakeFluidHandler.INSTANCE
        }

    override fun <T> getCapability(cap: Capability<T>, side: net.minecraft.core.Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
             return LazyOptional.of { this }.cast()
        }
        return super.getCapability(cap, side)
    }

    override fun getTanks(): Int {
        return fluidHandler.tanks
    }

    override fun getFluidInTank(tank: Int): FluidStack {
        return fluidHandler.getFluidInTank(tank)
    }

    override fun getTankCapacity(tank: Int): Int {
        return fluidHandler.getTankCapacity(tank)
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        return fluidHandler.isFluidValid(tank, stack)
    }

    override fun fill(resource: FluidStack, action: FluidAction): Int {
        return fluidHandler.fill(resource, action)
    }

    override fun drain(resource: FluidStack, action: FluidAction): FluidStack {
        return fluidHandler.drain(resource, action)
    }

    override fun drain(maxDrain: Int, action: FluidAction): FluidStack {
        return fluidHandler.drain(maxDrain, action)
    }

    override fun getCableRender(side: Direction, lrdu: LRDU): CableRenderDescriptor? {
        return if (elementRender == null) null else elementRender!!.getCableRenderSide(side, lrdu)
    }

    override fun serverPublishUnserialize(stream: DataInputStream) {
        super.serverPublishUnserialize(stream)
        try {
            val id = stream.readShort()
            if (id.toInt() == 0) {
                elementRenderId = 0.toShort()
                elementRender = null
            } else {
                if (id != elementRenderId) {
                    elementRenderId = id
                    val descriptor = Eln.transparentNodeItem.getDescriptor(id.toInt())
                    elementRender = descriptor!!.RenderClass.getConstructor(TransparentNodeBlockEntity::class.java, TransparentNodeDescriptor::class.java).newInstance(this, descriptor) as TransparentNodeElementRender
                }
                elementRender!!.networkUnserialize(stream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        val n = node as TransparentNode? ?: return null
        return n.newContainer(side, player)
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen? {
        return elementRender!!.newGuiDraw(side, player)
    }

    override fun preparePacketForServer(stream: DataOutputStream) {
        try {
            super.preparePacketForServer(stream)
            stream.writeShort(elementRenderId.toInt())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun sendPacketToServer(bos: ByteArrayOutputStream?) {
        super.sendPacketToServer(bos)
    }

    override fun cameraDrawOptimisation(): Boolean {
        return if (elementRender == null) super.cameraDrawOptimisation() else elementRender!!.cameraDrawOptimisation()
    }

    @Suppress("UNUSED_PARAMETER") fun getDamageValue(world: Level, x: Int, y: Int, z: Int): Int {
        return if (world.isClientSide) {
            elementRenderId.toInt()
        } else 0
    }

    override fun tileEntityNeighborSpawn() {
        if (elementRender != null) elementRender!!.notifyNeighborSpawn()
    }

    /*
    fun addCollisionBoxesToList(par5AABB: AABB, list: MutableList<AABB?>, blockCoord: Coordinate?) {
        // Legacy collision code
    }
    */

    override fun serverPacketUnserialize(stream: DataInputStream) {
        super.serverPacketUnserialize(stream)
        if (elementRender != null) elementRender!!.serverPacketUnserialize(stream)
    }

    override val nodeUuid: String
        get() = "t"

    override fun destructor() {
        if (elementRender != null) elementRender!!.destructor()
        super.destructor()
    }

    override fun clientRefresh(deltaT: Float) {
        if (elementRender != null) {
            elementRender!!.refresh(deltaT)
        }
    }

    override fun isProvidingWeakPower(side: Direction?): Int {
        return 0
    }

    open val sidedInventory: WorldlyContainer
        get() {
            if (level!!.isClientSide) {
                if (elementRender == null) return instance
                val i = elementRender!!.inventory
                if (i != null && i is WorldlyContainer) {
                    return i
                }
            } else {
                val node = node
                if (node != null && node is TransparentNode) {
                    val i = node.getInventory(null)
                    if (i != null && i is WorldlyContainer) {
                        return i
                    }
                }
            }
            return instance
        }

    override fun getContainerSize(): Int {
        return sidedInventory.containerSize
    }

    override fun getItem(var1: Int): ItemStack {
        return sidedInventory.getItem(var1)
    }

    override fun removeItem(var1: Int, var2: Int): ItemStack {
        return sidedInventory.removeItem(var1, var2)
    }

    override fun removeItemNoUpdate(var1: Int): ItemStack {
        return sidedInventory.removeItemNoUpdate(var1)
    }

    override fun setItem(var1: Int, var2: ItemStack) {
        sidedInventory.setItem(var1, var2)
    }

    override fun getMaxStackSize(): Int {
        return sidedInventory.maxStackSize
    }

    override fun stillValid(var1: Player): Boolean {
        return sidedInventory.stillValid(var1)
    }

    override fun startOpen(player: Player) {
        sidedInventory.startOpen(player)
    }

    override fun stopOpen(player: Player) {
        sidedInventory.stopOpen(player)
    }

    override fun canPlaceItem(var1: Int, var2: ItemStack): Boolean {
        return sidedInventory.canPlaceItem(var1, var2)
    }

    override fun getSlotsForFace(side: net.minecraft.core.Direction): IntArray {
        return sidedInventory.getSlotsForFace(side)
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: net.minecraft.core.Direction?): Boolean {
        return sidedInventory.canPlaceItemThroughFace(index, itemStack, direction)
    }

    override fun canTakeItemThroughFace(index: Int, itemStack: ItemStack, direction: net.minecraft.core.Direction): Boolean {
        return sidedInventory.canTakeItemThroughFace(index, itemStack, direction)
    }

    override fun clearContent() {
        sidedInventory.clearContent()
    }

    override fun isEmpty(): Boolean {
        return sidedInventory.isEmpty
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        if (tag.contains("eid")) {
            val node = TransparentNode()
            node.coordinate = Coordinate(this)
            node.level = level
            node.readFromNBT(tag)
            this.internalNode = node
            NodeManager.instance?.addNode(node)
        }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        (internalNode as? TransparentNode)?.writeToNBT(tag)
    }
}

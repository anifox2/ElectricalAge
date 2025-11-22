package mods.eln.node.transparent

import mods.eln.Eln
import mods.eln.item.IConfigurable
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Utils.println
import mods.eln.node.Node
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraftforge.fluids.capability.IFluidHandler
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException

class TransparentNode : Node() {
    @JvmField
    var element: TransparentNodeElement? = null
    @JvmField
    var elementId = 0
    @JvmField
    var removedByPlayer: ServerPlayer? = null
    override fun nodeAutoSave(): Boolean {
        return false
    }

    override fun onNeighborBlockChange() {
        super.onNeighborBlockChange()
        element!!.onNeighborBlockChange()
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt.getCompound("node"))
        elementId = nbt.getShort("eid").toInt()
        try {
            val descriptor = Eln.transparentNodeItem.getDescriptor(elementId)
            element = descriptor!!.ElementClass.getConstructor(TransparentNode::class.java, TransparentNodeDescriptor::class.java).newInstance(this, descriptor) as TransparentNodeElement
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
        element!!.readFromNBT(nbt.getCompound("element"))
    }

    override fun writeToNBT(nbt: CompoundTag) {
        val nodeTag = CompoundTag()
        nbt.put("node", nodeTag)
        super.writeToNBT(nodeTag)
        
        nbt.putShort("eid", elementId.toShort())
        
        val elementTag = CompoundTag()
        nbt.put("element", elementTag)
        element!!.writeToNBT(elementTag)
    }

    override fun onBreakBlock() {
        element!!.onBreakElement()
        super.onBreakBlock()
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU, mask: Int): ElectricalLoad? {
        return element!!.getElectricalLoad(side, lrdu)
    }

    override fun getThermalLoad(side: Direction, lrdu: LRDU, mask: Int): ThermalLoad? {
        return element!!.getThermalLoad(side, lrdu)
    }

    override fun getSideConnectionMask(side: Direction, lrdu: LRDU): Int {
        return element!!.getConnectionMask(side, lrdu)
    }

    override fun multiMeterString(side: Direction): String {
        return element!!.multiMeterString(side)
    }

    override fun thermoMeterString(side: Direction): String {
        return element!!.thermoMeterString(side)
    }

    override fun readConfigTool(side: Direction?, tag: CompoundTag?, invoker: Player?): Boolean {
        if (element is IConfigurable && tag != null && invoker != null) {
            (element as IConfigurable).readConfigTool(tag, invoker)
            return true
        }
        return false
    }

    override fun writeConfigTool(side: Direction?, tag: CompoundTag?, invoker: Player?): Boolean {
        if (element is IConfigurable && tag != null && invoker != null) {
            (element as IConfigurable).writeConfigTool(tag, invoker)
            return true
        }
        return false
    }

    val fluidHandler: net.minecraftforge.fluids.capability.IFluidHandler?
        get() = element!!.getFluidHandler()

    override fun publishSerialize(stream: DataOutputStream) {
        super.publishSerialize(stream)
        try {
            stream.writeShort(elementId)
            element!!.networkSerialize(stream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    enum class FrontType {
        BlockSide, PlayerView, PlayerViewHorizontal, BlockSideInv
    }

    override fun initializeFromThat(front: Direction, entityLiving: LivingEntity?, itemStack: ItemStack?) {
        try {
            val descriptor = Eln.transparentNodeItem.getDescriptor(itemStack)
            val metadata = itemStack!!.getOrCreateTag().getInt("elementId")
            elementId = metadata
            element = descriptor!!.ElementClass.getConstructor(TransparentNode::class.java, TransparentNodeDescriptor::class.java).newInstance(this, descriptor) as TransparentNodeElement
            element!!.initializeFromThat(front, entityLiving, itemStack.tag)
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
        println("TN.iFT element = $element elId = $elementId")
    }

    override fun initializeFromNBT() {
        element!!.initialize()
    }

    override fun onBlockActivated(entityPlayer: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        return if (element!!.onBlockActivated(entityPlayer, side, vx, vy, vz)) true else super.onBlockActivated(entityPlayer, side, vx, vy, vz)
    }

    override fun hasGui(side: Direction): Boolean {
        return if (element == null) false else element!!.hasGui()
    }

    fun getInventory(@Suppress("UNUSED_PARAMETER") side: Direction?): Container? {
        return if (element == null) null else element!!.inventory
    }

    fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return if (element == null) null else element!!.newContainer(side, player)
    }

    override val blockMetadata: Int
        get() {
            println("TN.gBM")
            println(element)
            println(element!!.transparentNodeDescriptor)
            return element!!.transparentNodeDescriptor.tileEntityMetaTag.meta
        }

    override fun networkUnserialize(stream: DataInputStream, player: ServerPlayer?) {
        super.networkUnserialize(stream, player)
        try {
            if (elementId == stream.readShort().toInt()) {
                element!!.networkUnserialize(stream, player)
            } else {
                println("Transparent node unserialize miss")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun connectJob() {
        super.connectJob()
        element!!.connectJob()
    }

    override fun disconnectJob() {
        super.disconnectJob()
        element!!.disconnectJob()
    }

    override fun checkCanStay(onCreate: Boolean) {
        super.checkCanStay(onCreate)
        element!!.checkCanStay(onCreate)
    }

    fun dropElement(entityPlayer: ServerPlayer?) {
        if (element != null) {
            if (entityPlayer == null || !entityPlayer.isCreative()) {
                dropItem(element!!.dropItemStack)
            }
        }
    }

    override val nodeUuid: String
        get() = "t"

    override fun unload() {
        super.unload()
        if (element != null) element!!.unload()
    }
}

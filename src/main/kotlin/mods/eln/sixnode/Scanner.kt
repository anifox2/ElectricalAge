package mods.eln.sixnode

import mods.eln.Eln
import mods.eln.cable.CableRenderDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.misc.*
import mods.eln.node.NodeBase
import mods.eln.node.six.*
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.nbt.NbtElectricalGateOutput
import mods.eln.sim.nbt.NbtElectricalGateOutputProcess
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.Direction as MCDirection
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.items.IItemHandler
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import net.minecraft.network.chat.Component

/**
 * A comparator-alike. It doesn't "compare" anything, though.
 */
class ScannerDescriptor(name: String, obj: Obj3D) : SixNodeDescriptor(name, ScannerElement::class.java, ScannerRender::class.java) {

    val main = obj.getPart("main")!!
    val leds = arrayOf("LED_0", "LED_1").map { obj.getPart(it) }.requireNoNulls()

    init {
        voltageLevelColor = VoltageLevelColor.SignalVoltage
    }

    fun draw(mode: ScanMode) {
        main.draw()
        leds[mode.value.toInt()].draw()
    }

    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(Component.literal(tr("Scans blocks to produce signals.")))
        list.add(Component.literal(tr("- For tanks, outputs fill percentage.")))
        list.add(Component.literal(tr("- For inventories, outputs either total fill or fraction of slots with any items.")))
        list.add(Component.literal(tr("Right-click to change mode.")))
        list.add(Component.literal(tr("Otherwise behaves as a vanilla comparator.")))
    }
}

enum class ScanMode(val value: Byte) {
    SIMPLE(0), SLOTS(1);

    companion object {
        private val map = ScanMode.entries.associateBy(ScanMode::value);
        fun fromByte(type: Byte) = map[type]
    }
}

class ScannerElement(_sixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElement(_sixNode, side, descriptor) {
    val output = NbtElectricalGateOutput("signal")
    val outputProcess = NbtElectricalGateOutputProcess("signalP", output)

    var mode = ScanMode.SIMPLE

    val updater = IProcess {
        val appliedLRDU = side.applyLRDU(front)
        val scannedCoord = Coordinate(coordinate!!).apply {
            move(appliedLRDU)
        }
        val targetSide: MCDirection = appliedLRDU.inverse().toMCDirection()
        val level = sixNode.entity.level!!
        val pos = scannedCoord.toBlockPos()
        val te = level.getBlockEntity(pos)
        
        var out: Double? = null
        if (te != null) {
            out = scanTileEntity(te, targetSide)
        }
        if (out == null) {
            out = scanBlock(level, pos, targetSide)
        }
        outputProcess.outputNormalized = out
    }

    init {
        electricalLoadList.add(output)
        electricalComponentList.add(outputProcess)
        slowProcessList.add(updater)
    }

    private fun scanBlock(level: net.minecraft.world.level.Level, pos: net.minecraft.core.BlockPos, targetSide: MCDirection): Double {
        val state = level.getBlockState(pos)
        return when {
            state.hasAnalogOutputSignal() -> state.getAnalogOutputSignal(level, pos) / 15.0
            state.isSolidRender(level, pos) -> 1.0
            state.isAir -> 0.0
            else -> 1.0/3.0
        }
    }

    private fun scanTileEntity(te: BlockEntity, side: MCDirection): Double? {
        val fluidCap = te.getCapability(ForgeCapabilities.FLUID_HANDLER, side).resolve()
        if (fluidCap.isPresent) {
            val handler = fluidCap.get()
            val tanks = handler.tanks
            if (tanks > 0) {
                var capacity = 0L
                var amount = 0L
                for (i in 0 until tanks) {
                    capacity += handler.getTankCapacity(i)
                    amount += handler.getFluidInTank(i).amount
                }
                if (capacity > 0) return amount.toDouble() / capacity.toDouble()
            }
        }

        val itemCap = te.getCapability(ForgeCapabilities.ITEM_HANDLER, side).resolve()
        if (itemCap.isPresent) {
            val handler = itemCap.get()
            val slots = handler.slots
            if (slots > 0) {
                if (mode == ScanMode.SLOTS) {
                    var filledSlots = 0
                    for (i in 0 until slots) {
                        if (!handler.getStackInSlot(i).isEmpty) filledSlots++
                    }
                    return filledSlots.toDouble() / slots.toDouble()
                } else {
                    var maxStack = 0
                    var currentStack = 0
                    for (i in 0 until slots) {
                        val stack = handler.getStackInSlot(i)
                        maxStack += handler.getSlotLimit(i)
                        if (!stack.isEmpty) {
                            currentStack += stack.count
                        }
                    }
                    if (maxStack > 0) return currentStack.toDouble() / maxStack.toDouble()
                }
            }
        }
        
        return null
    }

    override fun onBlockActivated(entityPlayer: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        if (onBlockActivatedRotate(entityPlayer)) return true
        // if (entityPlayer.isHoldingMeter()) return false // isHoldingMeter missing?
        mode = if (mode == ScanMode.SIMPLE) ScanMode.SLOTS else ScanMode.SIMPLE
        Utils.addChatMessage(entityPlayer, "Scanner mode: " + mode.name)
        needPublish()
        return true
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        stream.writeByte(mode.value.toInt())
    }

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        mode = ScanMode.fromByte(stream.readByte()) ?: ScanMode.SIMPLE
    }
    
    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? = output
    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? = null

    override fun getConnectionMask(lrdu: LRDU) = when (lrdu) {
        front.inverse() -> NodeBase.maskElectricalOutputGate
        else -> 0
    }

    override fun multiMeterString(): String {
        return "Mode: ${tr(mode.name.lowercase()
            .replaceFirstChar { it.titlecase(Locale.getDefault()) })}, Value: ${Utils.plotPercent("", outputProcess.outputNormalized)}"
    }

    override fun thermoMeterString(): String = ""

    override fun initialize() {
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putByte("mode", mode.value)
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        mode = ScanMode.fromByte(nbt.getByte("mode"))!!
    }
}

class ScannerRender(tileEntity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElementRender(tileEntity, side, descriptor) {
    var mode = ScanMode.SIMPLE

    override fun draw() {
        super.draw()
        front!!.glRotateOnX()
        (sixNodeDescriptor as ScannerDescriptor).draw(mode)
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        mode = ScanMode.fromByte(stream.readByte()) ?: ScanMode.SIMPLE
    }
    
    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? = Eln.instance.signalCableDescriptor.render
}

package mods.eln.item.electricalitem

import mods.eln.Eln
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.item.electricalinterface.IItemEnergyBattery
//import mods.eln.misc.Obj3D
//import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.misc.Utils
//import mods.eln.misc.UtilsClient
import mods.eln.wiki.Data
import net.minecraft.world.level.block.Block
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionHand
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import mods.eln.misc.nbt
import mods.eln.misc.getDouble
import mods.eln.misc.putDouble
import mods.eln.misc.getByte
import mods.eln.misc.putByte
import mods.eln.misc.getShort
import mods.eln.misc.setShort
import net.minecraft.core.BlockPos

// @ExperimentalUnsignedTypes
class PortableOreScannerItem(name: String?,
                             var energyStorage: Double, var chargePower: Double, var dischargePower: Double,
                             var viewRange: Float, var viewYAlpha: Float, var resWidth: Int, var resHeight: Int) : GenericItemUsingDamageDescriptor(name!!), IItemEnergyBattery {

    /*
    var base: Obj3DPart = obj.getPart("Base")
    var led: Obj3DPart = obj.getPart("Led")
    var ledHalo: Obj3DPart = obj.getPart("LedHalo")
    var textBat: Array<Obj3DPart> = (0..3).map { obj.getPart("TextBat$it") }.toTypedArray()
    var textRun: Obj3DPart = obj.getPart("TextRun")
    var textInit: Obj3DPart = obj.getPart("TextInit")
    var buttons: Obj3DPart = obj.getPart("Buttons")
    var screenDamage: Array<Obj3DPart> = (0..2).map { obj.getPart("ScreenDamageL" + (it + 1)) }.toTypedArray()
    var screenLuma: Obj3DPart = obj.getPart("ScreenLuma")
    */
    private val damagePerBreakLevel: Byte = 3

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        if (level.isClientSide) return
        if (entity !is ServerPlayer) return
        val state = getState(stack)
        var counter = getCounter(stack)

        if (getDamage(stack) / damagePerBreakLevel >= 4) {
            if (state != State.Idle)
                setState(stack, State.Idle)
            return
        }

        when (state) {
            State.Boot -> if ((--counter).toInt() != 0) {
                setCounter(stack, counter)
            } else {
                setState(stack, State.Run)
            }
            State.Stop -> if ((--counter).toInt() != 0) {
                setCounter(stack, counter)
            } else {
                setState(stack, State.Idle)
            }
            else -> {}
        }
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        if (level.isClientSide) return InteractionResultHolder.pass(stack)
        val energy = getEnergy(stack)
        val state = getState(stack)

        when (state) {
            State.Idle -> if (energy > dischargePower) {
                setState(stack, State.Boot)
                setCounter(stack, bootTime)
            }
            State.Run -> {
                setState(stack, State.Stop)
                setCounter(stack, stopTime)
            }
            else -> {}
        }
        return InteractionResultHolder.success(stack)
    }

    override fun setParent(registry: Any?, damage: Int) {
        super.setParent(registry, damage)
        // Data.addPortable(newItemStack()) // Commented out due to unresolved reference
    }

    override fun getDefaultNBT(): CompoundTag? {
        val nbt = CompoundTag()
        nbt.putDouble("e", energyStorage * 0.2)
        nbt.putByte("s", State.Boot.serialized)
        nbt.putShort("c", bootTime)
        nbt.putByte("d", 0.toByte())
        return nbt
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, level, list, flag)
        list.add(Component.literal(tr("Discharge power: %1\$W", Utils.plotValue(dischargePower))))
        
        list.add(Component.literal(tr("Stored energy: %1\$J (%2$%)", Utils.plotValue(getEnergy(stack)),
            (getEnergy(stack) / energyStorage * 100).toInt())))
    }

    override fun getEnergy(stack: ItemStack): Double {
        return stack.getDouble("e")
    }

    override fun setEnergy(stack: ItemStack, value: Double) {
        stack.putDouble("e", value)
    }

    private fun getState(stack: ItemStack): State {
        return State.from(stack.getByte("s")) ?: State.Idle
    }

    private fun setState(stack: ItemStack, value: State) {
        stack.putByte("s", value.serialized)
    }

    fun getCounter(stack: ItemStack): Short {
        return stack.getShort("c")
    }

    fun setCounter(stack: ItemStack, value: Short) {
        stack.setShort("c", value)
    }

    fun getDamage(stack: ItemStack): Byte {
        return stack.getByte("d")
    }

    fun setDamage(stack: ItemStack, value: Byte) {
        stack.putByte("d", value)
    }

    override fun onDroppedByPlayer(item: ItemStack, player: Player): Boolean {
        setState(item, State.Idle)
        return super.onDroppedByPlayer(item, player)
    }

    override fun getEnergyMax(stack: ItemStack): Double {
        return energyStorage
    }

    override fun getTransferRate(stack: ItemStack): Double {
        return chargePower
    }

    override fun getChargePower(stack: ItemStack): Double {
        return chargePower
    }

    fun getDischagePower(stack: ItemStack): Double {
        return dischargePower
    }

    fun getPriority(stack: ItemStack): Int {
        return 0
    }

    override fun onBlockStartBreak(itemstack: ItemStack, pos: BlockPos, player: Player): Boolean {
        if (!player.level().isClientSide) {
            setDamage(itemstack, (getDamage(itemstack) + 1).toByte())
            //Utils.println("Break");
        }
        return false
    }

    fun electricalItemUpdate(stack: ItemStack, time: Double) {
        var energy = getEnergy(stack)
        val state = getState(stack)
        when (state) {
            State.Idle -> {
            }
            else -> {
                energy -= dischargePower * time
                if (energy <= 0) {
                    setState(stack, State.Idle)
                    setEnergy(stack, 0.0)
                    return
                }
                setEnergy(stack, energy)
            }
        }
    }
}

private enum class State(val serialized: Byte) {
    Idle(0),
    Boot(1),
    Run(2),
    Stop(3),
    Error(4);

    companion object {
        private val map = State.values().associateBy(State::serialized)
        fun from(type: Byte) = map[type]
    }
}

private const val bootTime: Short = (4 * 20).toShort()
private const val stopTime: Short = (1 * 20).toShort()

/*
@ExperimentalUnsignedTypes
object OreColorMapping {
    val map: FloatArray
        get() {
            return if (cache == null) {
                updateColorMapping()
            } else {
                cache!!
            }
        }

    private var cache: FloatArray? = null

    fun updateColorMapping(): FloatArray {
        val blockKeyMapping = FloatArray(1024 * 64)
        for (blockId in 0..4095) {
            for (meta in 0..15) {
                blockKeyMapping[blockId + (meta shl 12)] = 0f
            }
        }

        for (c in Eln.oreScannerConfig) {
            if (c.blockKey >= 0 && c.blockKey < blockKeyMapping.size)
                blockKeyMapping[c.blockKey] = c.factor
        }

        cache = blockKeyMapping
        return blockKeyMapping
    }
}
*/


package mods.eln.item.electricalitem

import mods.eln.Eln
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.item.electricalinterface.IItemEnergyBattery
import mods.eln.misc.*
import net.minecraft.world.level.block.Block
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

open class ElectricalTool(name: String, var strengthOn: Float, var strengthOff: Float,
                          var energyStorage: Double, var energyPerBlock: Double, var chargePower: Double) : GenericItemUsingDamageDescriptor(name), IItemEnergyBattery {
    var light = 0
    var range = 0
    
    override fun onEntitySwing(stack: ItemStack, entityLiving: LivingEntity): Boolean {
        if (entityLiving.level().isClientSide) return false
        //Eln.itemEnergyInventoryProcess.addExclusion(this, 2.0)
        return super.onEntitySwing(stack, entityLiving)
    }

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, entity: LivingEntity): Boolean {
        subtractEnergyForBlockBreak(stack, state)
        Utils.println("destroy")
        return true
    }

    fun subtractEnergyForBlockBreak(stack: ItemStack, state: BlockState) {
        if (getDestroySpeed(stack, state) == strengthOn) {
            var e = getEnergy(stack) - energyPerBlock
            if (e < 0) e = 0.0
            setEnergy(stack, e)
        }
    }

    fun getStrength(stack: ItemStack): Float {
        return if (getEnergy(stack) >= energyPerBlock) strengthOn else strengthOff
    }

    override fun getDefaultNBT(): CompoundTag? {
        val nbt = CompoundTag()
        nbt.putDouble("energy", 0.0)
        nbt.putBoolean("powerOn", false)
        nbt.putInt("rand", (Math.random() * 0xFFFFFFF).toInt())
        return nbt
    }

    fun getPowerOn(stack: ItemStack): Boolean {
        return stack.orCreateTag.getBoolean("powerOn")
    }

    fun setPowerOn(stack: ItemStack, value: Boolean) {
        stack.orCreateTag.putBoolean("powerOn", value)
    }

    override fun appendHoverText(itemStack: ItemStack, level: Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(net.minecraft.network.chat.Component.literal(tr("Stored energy: %s (%d%%)", Utils.plotValue(getEnergy(itemStack)),
            (getEnergy(itemStack) / getEnergyMax(itemStack) * 100).toInt())))
    }

    override fun getEnergy(stack: ItemStack): Double {
        return stack.orCreateTag.getDouble("energy")
    }

    override fun setEnergy(stack: ItemStack, value: Double) {
        stack.orCreateTag.putDouble("energy", value)
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
        return 0.0
    }

    fun getPriority(stack: ItemStack): Int {
        return 0
    }

    fun electricalItemUpdate(stack: ItemStack, time: Double) {}

    companion object {
        val blocksEffectiveAgainst = arrayOf(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND, Blocks.GRAVEL, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.CLAY, Blocks.FARMLAND, Blocks.SOUL_SAND, Blocks.MYCELIUM)
    }

    init {
        // rIcon = ResourceLocation("eln", "textures/items/" + name.replace(" ", "").lowercase() + ".png")
        iconName = "eln:textures/items/" + name.replace(" ", "").lowercase() + ".png"
    }
}

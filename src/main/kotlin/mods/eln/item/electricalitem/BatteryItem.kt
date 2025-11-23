package mods.eln.item.electricalitem

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.item.electricalinterface.IItemEnergyBattery
import mods.eln.misc.Utils
//import mods.eln.misc.UtilsClient
import mods.eln.wiki.Data
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import mods.eln.misc.nbt
import mods.eln.misc.getDouble
import mods.eln.misc.putDouble

class BatteryItem(name: String, var energyStorage: Double, var chargePower: Double, var dischargePower: Double, private val priority: Int) : GenericItemUsingDamageDescriptor(name), IItemEnergyBattery {

    override fun setParent(registry: Any?, id: Int) {
        super.setParent(registry, id)
        //Data.addPortable(newItemStack())
    }

    override fun getDefaultNBT(): CompoundTag? {
        val nbt = CompoundTag()
        nbt.putDouble("energy", 0.0)
        return nbt
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, level, list, flag)
        list.add(Component.literal(tr("Charge power: %1\$W", Utils.plotValue(chargePower))))
        list.add(Component.literal(tr("Discharge power: %1\$W", Utils.plotValue(dischargePower))))
        
        list.add(Component.literal(tr("Stored energy: %1\$J (%2$%)", Utils.plotValue(getEnergy(stack)),
            (getEnergy(stack) / energyStorage * 100).toInt())))
    }

    override fun getEnergy(stack: ItemStack): Double {
        return stack.getDouble("energy")
    }

    override fun setEnergy(stack: ItemStack, value: Double) {
        stack.putDouble("energy", Math.max(0.0, value))
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
        return priority
    }

    fun electricalItemUpdate(stack: ItemStack, time: Double) {}
}

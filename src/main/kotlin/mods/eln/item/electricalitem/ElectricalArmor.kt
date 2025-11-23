package mods.eln.item.electricalitem

import mods.eln.generic.genericArmorItem
import mods.eln.i18n.I18N.tr
import mods.eln.item.electricalinterface.IItemEnergyBattery
import mods.eln.misc.Utils
import mods.eln.wiki.Data
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.damagesource.DamageSource
//import net.minecraftforge.common.ISpecialArmor
//import net.minecraftforge.common.ISpecialArmor.ArmorProperties
import net.minecraft.world.item.ArmorMaterial

class ElectricalArmor(
    par2EnumArmorMaterial: ArmorMaterial,
    par3: Int,
    type: ArmourType,
    t1: String,
    t2: String,  //String icon,
    var energyStorage: Double,
    var chargePower: Double,
    var ratioMax: Double,
    var ratioMaxEnergy: Double,
    var energyPerDamage: Double
    ) : genericArmorItem(par2EnumArmorMaterial, par3, type, t1, t2), IItemEnergyBattery { //, ISpecialArmor {


    /*override fun getProperties(player: LivingEntity, armor: ItemStack, source: DamageSource, damage: Double, slot: Int): ArmorProperties {
        return ArmorProperties(100, Math.min(1.0, getEnergy(armor) / ratioMaxEnergy) * ratioMax, (getEnergy(armor) / energyPerDamage * 25.0).toInt())
    }

    override fun getArmorDisplay(player: Player, armor: ItemStack, slot: Int): Int {
        return (Math.min(1.0, getEnergy(armor) / ratioMaxEnergy) * ratioMax * 20).toInt()
    }

    override fun damageArmor(entity: LivingEntity, stack: ItemStack, source: DamageSource, damage: Int, slot: Int) {
        var e = getEnergy(stack)
        e = Math.max(0.0, e - damage * energyPerDamage)
        setEnergy(stack, e)
        Utils.println("armor hit  damage=" + damage + " energy=" + e + " energyLost=" + damage * energyPerDamage)
    }*/

    override fun isValidRepairItem(toRepair: ItemStack, repair: ItemStack): Boolean {
        return false
    }

    /*
    override fun hasColor(par1ItemStack: ItemStack): Boolean {
        return false
    }
    */

    val defaultNBT: CompoundTag
        get() {
            val nbt = CompoundTag()
            nbt.putDouble("energy", 0.0)
            nbt.putBoolean("powerOn", false)
            nbt.putInt("rand", (Math.random() * 0xFFFFFFF).toInt())
            return nbt
        }

    protected fun getNbt(stack: ItemStack): CompoundTag {
        return stack.orCreateTag
    }

    fun getPowerOn(stack: ItemStack): Boolean {
        return getNbt(stack).getBoolean("powerOn")
    }

    fun setPowerOn(stack: ItemStack, value: Boolean) {
        getNbt(stack).putBoolean("powerOn", value)
    }

    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(net.minecraft.network.chat.Component.literal(tr("Charge power: %1\$W", chargePower.toInt())))
        list.add(net.minecraft.network.chat.Component.literal(tr("Stored energy: %1\$J (%2$%)", getEnergy(itemStack),
            (getEnergy(itemStack) / energyStorage * 100).toInt())))
        //list.add("Power button is " + (getPowerOn(itemStack) ? "ON" : "OFF"));
    }

    override fun getEnergy(stack: ItemStack): Double {
        return getNbt(stack).getDouble("energy")
    }

    override fun setEnergy(stack: ItemStack, value: Double) {
        getNbt(stack).putDouble("energy", value)
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

    override fun getEnchantmentValue(): Int {
        return 0;
    }

    init {
        //rIcon = new ResourceLocation("eln", icon);
        //Data.addPortable(ItemStack(this))
    }
}

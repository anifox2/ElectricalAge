package mods.eln.item.electricalinterface

import net.minecraft.world.item.ItemStack

interface IItemEnergyBattery {
    fun getEnergy(stack: ItemStack): Double
    fun setEnergy(stack: ItemStack, energy: Double)
    fun getEnergyMax(stack: ItemStack): Double
    fun getChargePower(stack: ItemStack): Double
    fun getTransferRate(stack: ItemStack): Double
}
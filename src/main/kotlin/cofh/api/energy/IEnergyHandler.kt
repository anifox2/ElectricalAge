package cofh.api.energy

import net.minecraft.core.Direction

interface IEnergyHandler {
    fun receiveEnergy(from: Direction?, maxReceive: Int, simulate: Boolean): Int
    fun extractEnergy(from: Direction?, maxExtract: Int, simulate: Boolean): Int
    fun getEnergyStored(from: Direction?): Int
    fun getMaxEnergyStored(from: Direction?): Int
    fun canConnectEnergy(from: Direction?): Boolean
}

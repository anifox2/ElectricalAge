package mods.eln.fluid

import mods.eln.misc.INBTTReady
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.fluids.capability.templates.FluidTank
import net.minecraft.world.level.material.Fluid

open class ElementSidedFluidHandler : INBTTReady {

    protected val tanks = mutableMapOf<Direction, TankData>()

    data class TankData(val tank: FluidTank, val fluidWhitelist: MutableList<Fluid>)

    constructor(tankData: Map<Direction, TankData>) {
        tanks.putAll(tankData)
        tanks.values.forEach { updateValidator(it) }
    }

    constructor(tankSizeMb: Int) {
        val tank = TankData(FluidTank(tankSizeMb), mutableListOf())
        Direction.values().forEach {
            tanks[it] = tank
        }
        updateValidator(tank)
    }

    fun getHandler(side: Direction?): IFluidHandler? {
        if (side == null) return null
        return tanks[side]?.tank
    }

    fun setFluidWhitelist(direction: Direction, fluidWhitelist: List<Fluid>) {
        val tank = tanks[direction]
        if (tank != null) {
            tank.fluidWhitelist.clear()
            tank.fluidWhitelist.addAll(fluidWhitelist)
            updateValidator(tank)
        }
    }

    fun addFluidWhitelist(direction: Direction, fluidWhitelist: Fluid) {
        val tank = tanks[direction]
        if (tank != null) {
            tank.fluidWhitelist.add(fluidWhitelist)
            updateValidator(tank)
        }
    }

    private fun updateValidator(tankData: TankData) {
        tankData.tank.setValidator { stack ->
            tankData.fluidWhitelist.isEmpty() || tankData.fluidWhitelist.contains(stack.fluid)
        }
    }

    fun getFluidType(direction: Direction): Fluid? {
        return tanks[direction]?.tank?.fluid?.fluid
    }

    fun getCapacity(direction: Direction): Int {
        return tanks[direction]?.tank?.capacity ?: 0
    }

    fun getFluidAmount(direction: Direction): Int {
        return tanks[direction]?.tank?.fluidAmount ?: 0
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        if (nbt.contains(str)) {
            val tag = nbt.getCompound(str)
            val uniqueTanks = tanks.values.map { it.tank }.distinct()
            uniqueTanks.forEachIndexed { index, tank ->
                if (tag.contains("tank_$index")) {
                    tank.readFromNBT(tag.getCompound("tank_$index"))
                }
            }
        }
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        val tag = CompoundTag()
        val uniqueTanks = tanks.values.map { it.tank }.distinct()
        uniqueTanks.forEachIndexed { index, tank ->
            val tankTag = CompoundTag()
            tank.writeToNBT(tankTag)
            tag.put("tank_$index", tankTag)
        }
        nbt.put(str, tag)
    }
}

package mods.eln.fluid

import mods.eln.misc.INBTTReady
import net.minecraft.nbt.CompoundTag
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.templates.FluidTank
import net.minecraft.world.level.material.Fluid

open class ElementFluidHandler(capacity: Int) : IFluidHandler, INBTTReady {
    val tank = FluidTank(capacity)
    private var whitelist: Array<Fluid>? = null
    private var fluidHeatMb = 0.0

    fun setFilter(whitelist: Array<Fluid>) {
        this.whitelist = whitelist
        tank.validator = { fluidStack ->
            whitelist.contains(fluidStack.fluid)
        }
    }

    fun getHeatEnergyPerMilliBucket(): Double {
        if (fluidHeatMb == 0.0 && !tank.fluid.isEmpty) {
            setHeatEnergyPerMilliBucket(tank.fluid.fluid)
        }
        return fluidHeatMb
    }

    private fun setHeatEnergyPerMilliBucket(fluid: Fluid) {
        fluidHeatMb = FuelRegistry.heatEnergyPerMilliBucket(fluid)
    }

    override fun getTanks() = tank.tanks
    override fun getFluidInTank(tank: Int) = tank.getFluidInTank(tank)
    override fun getTankCapacity(tank: Int) = tank.getTankCapacity(tank)
    override fun isFluidValid(tank: Int, stack: FluidStack) = tank.isFluidValid(tank, stack)

    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int {
        if (resource.isEmpty) return 0
        if (tank.fluidAmount == 0) {
             setHeatEnergyPerMilliBucket(resource.fluid)
        }
        return tank.fill(resource, action)
    }

    override fun drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack {
        return tank.drain(resource, action)
    }

    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack {
        return tank.drain(maxDrain, action)
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        if (nbt.contains(str)) {
            tank.readFromNBT(nbt.getCompound(str))
        }
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        val tag = CompoundTag()
        tank.writeToNBT(tag)
        nbt.put(str, tag)
    }
}

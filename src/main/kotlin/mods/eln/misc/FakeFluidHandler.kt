package mods.eln.misc

import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

class FakeFluidHandler : IFluidHandler {
    override fun getTanks(): Int {
        return 0
    }

    override fun getFluidInTank(tank: Int): FluidStack {
        return FluidStack.EMPTY
    }

    override fun getTankCapacity(tank: Int): Int {
        return 0
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        return false
    }

    override fun fill(resource: FluidStack, action: FluidAction): Int {
        return 0
    }

    override fun drain(resource: FluidStack, action: FluidAction): FluidStack {
        return FluidStack.EMPTY
    }

    override fun drain(maxDrain: Int, action: FluidAction): FluidStack {
        return FluidStack.EMPTY
    }

    companion object {
        val INSTANCE = FakeFluidHandler()
    }
}

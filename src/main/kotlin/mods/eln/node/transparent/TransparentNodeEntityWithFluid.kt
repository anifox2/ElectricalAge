package mods.eln.node.transparent

import mods.eln.misc.FakeFluidHandler
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

/**
 * Proxy class for TNEs with Forge fluids.
 */
class TransparentNodeEntityWithFluid : TransparentNodeEntity(), IFluidHandler {
    private val fluidHandler: IFluidHandler
        get() {
            if (level != null && !level!!.isRemote) {
                val node = node
                if (node != null && node is TransparentNode) {
                    val i = node.fluidHandler
                    if (i != null) {
                        return i
                    }
                }
            }
            return FakeFluidHandler.INSTANCE
        }

    override fun getTanks(): Int {
        return fluidHandler.tanks
    }

    override fun getFluidInTank(tank: Int): FluidStack {
        return fluidHandler.getFluidInTank(tank)
    }

    override fun getTankCapacity(tank: Int): Int {
        return fluidHandler.getTankCapacity(tank)
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        return fluidHandler.isFluidValid(tank, stack)
    }

    override fun fill(resource: FluidStack, action: FluidAction): Int {
        return fluidHandler.fill(resource, action)
    }

    override fun drain(resource: FluidStack, action: FluidAction): FluidStack {
        return fluidHandler.drain(resource, action)
    }

    override fun drain(maxDrain: Int, action: FluidAction): FluidStack {
        return fluidHandler.drain(maxDrain, action)
    }

    private class FakeFluidHandler : IFluidHandler {
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
            var INSTANCE = FakeFluidHandler()
        }
    }
}


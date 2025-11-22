package ic2.api.energy.tile

import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.Direction

interface IEnergySource {
    fun emitsEnergyTo(receiver: BlockEntity?, direction: Direction?): Boolean
    fun getOfferedEnergy(): Double
    fun drawEnergy(amount: Double)
    fun getSourceTier(): Int
}

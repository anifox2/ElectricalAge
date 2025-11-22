package ic2.api.energy.event

import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.eventbus.api.Event

class EnergyTileLoadEvent(val energyTile: BlockEntity) : Event()

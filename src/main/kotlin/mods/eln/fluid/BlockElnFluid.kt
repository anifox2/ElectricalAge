package mods.eln.fluid

import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.FlowingFluid
import java.util.function.Supplier

class BlockElnFluid(
    fluidSupplier: Supplier<out FlowingFluid>,
    properties: BlockBehaviour.Properties
) : LiquidBlock(fluidSupplier, properties)


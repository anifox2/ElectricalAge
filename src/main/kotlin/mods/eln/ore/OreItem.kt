package mods.eln.ore

import mods.eln.generic.GenericItemBlockUsingDamage
import net.minecraft.world.level.block.Block

class OreItem(b: Block?) : GenericItemBlockUsingDamage<OreDescriptor>(b!!) {
    
    /*
    override fun addDescriptor(damage: Int, descriptor: OreDescriptor) {
        super.addDescriptor(damage, descriptor)
        // GameRegistry.registerWorldGenerator(descriptor, 0)
    }
    */
}

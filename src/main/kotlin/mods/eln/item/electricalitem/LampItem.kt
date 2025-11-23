package mods.eln.item.electricalitem

import mods.eln.Eln
import mods.eln.generic.GenericItemUsingDamageDescriptor
// import mods.eln.sixnode.lampsocket.LightBlockEntity
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.ItemStack
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.core.BlockPos

abstract class LampItem(name: String?) : GenericItemUsingDamageDescriptor(name!!) {
    abstract fun getLightState(stack: ItemStack): Int
    abstract fun getRange(stack: ItemStack): Int
    abstract fun getLight(stack: ItemStack): Int
    
    // override fun onUpdate(stack: ItemStack, world: Level, entity: Entity, par4: Int, par5: Boolean) {
        // Logic commented out due to missing LightBlockEntity and porting issues
    // }
}

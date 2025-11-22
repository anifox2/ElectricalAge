package mods.eln.ore

import mods.eln.Eln
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player

class OreBlock : Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f, 5.0f)) {
    
    // Loot tables should handle drops now.
    // Models should handle rendering.
    
    /*
    // ...existing code...
    override fun damageDropped(i: Int): Int {
        return i
    }
    // ...existing code...
    */
}


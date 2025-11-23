package mods.eln.ore

import mods.eln.Eln
import mods.eln.generic.GenericItemBlockUsingDamageDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.*

class OreDescriptor(
    name: String, var metadata: Int,
    var spawnRate: Int, var spawnSizeMin: Int, var spawnSizeMax: Int, var spawnHeightMin: Int, var spawnHeightMax: Int
) : GenericItemBlockUsingDamageDescriptor(name) {

    // Rendering is handled by models now
    /*
    fun getBlockIconId(side: Int, damage: Int): IIcon {
        return icon
    }
    */

    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addResource(newItemStack())
    }

    fun getBlockDropped(fortune: Int): ArrayList<ItemStack> {
        val list = ArrayList<ItemStack>()
        // ItemStack no longer supports metadata in constructor. 
        // If this is for variants, we need to handle it differently (e.g. separate items or NBT)
        // For now, just creating the item.
        val stack = Eln.oreItem!!.newItemStack(1)
        // stack.damageValue = metadata // Only if it's durability damage
        list.add(stack)
        return list
    }

    /*
    // World generation is now data-driven (PlacedFeatures, etc.)
    override fun generate(
        random: Random, chunkX: Int, chunkZ: Int, world: World,
        chunkGenerator: IChunkProvider?, chunkProvider: IChunkProvider?
    ) {
       // ...
    }
    */
}


package mods.eln.block

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item

class ArcClayBlock : Block(BlockBehaviour.Properties.of().strength(2.0f)) {
    companion object {
        const val name = "arc_clay_block"
    }
}

class ArcMetalBlock : Block(BlockBehaviour.Properties.of().strength(2.0f)) {
    companion object {
        const val name = "arc_metal_block"
    }
}

class ArcMetalItemBlock(block: Block) : BlockItem(block, Item.Properties())

class ArcClayItemBlock(block: Block) : BlockItem(block, Item.Properties())

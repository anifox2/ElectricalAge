package mods.eln.simplenode.energyconverter

import mods.eln.misc.Direction
import mods.eln.node.simple.SimpleNode
import mods.eln.node.simple.SimpleNodeBlock
import mods.eln.node.simple.SimpleNodeEntity
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.Level

class EnergyConverterElnToOtherBlock(private val descriptor: EnergyConverterElnToOtherDescriptor) : SimpleNodeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.ICE)) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return EnergyConverterElnToOtherEntity(pos, state)
    }

    override fun newNode(): SimpleNode {
        return EnergyConverterElnToOtherNode()
    }

    init {
        setDescriptor(descriptor)
    }
}

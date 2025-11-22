package mods.eln.simplenode.energyconverter

import cofh.api.energy.IEnergyHandler
import mods.eln.Other
import mods.eln.misc.Direction
import net.minecraft.world.level.block.entity.BlockEntity

object EnergyConverterElnToOtherFireWallRf {

    fun updateEntity(e: EnergyConverterElnToOtherEntity) {
        if (e.level!!.isClientSide) return
        if (e.node == null) return
        val node = e.node as EnergyConverterElnToOtherNode

        val energySinkList: List<Pair<IEnergyHandler, Direction>> = Direction.values()
            .mapNotNull { dir ->
                val pos = e.blockPos.relative(dir.toMCDirection())
                val te = e.level!!.getBlockEntity(pos)
                if (te is IEnergyHandler) Pair(te, dir) else null
            }
        
        if (energySinkList.isEmpty()) return
        val rfUsed = energySinkList.map {
            val rfAvailable = (node.availableEnergyInModUnits(Other.getWattsToRf()) / energySinkList.size)
            // receiveEnergy takes RF in, gives out RF
            val rfUsed = it.first.receiveEnergy(it.second.toMCDirection(), rfAvailable.toInt(), false).toDouble()
            rfUsed
        }.sum()
        node.drawEnergy(rfUsed, Other.getWattsToRf())
    }
}

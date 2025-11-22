package mods.eln.integration.waila

import com.google.common.cache.CacheLoader
import mcp.mobius.waila.api.IWailaConfigHandler
import mcp.mobius.waila.api.IWailaDataAccessor
import mcp.mobius.waila.api.IWailaDataProvider
import mcp.mobius.waila.api.SpecialChars
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import net.minecraft.world.entity.player.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.level.Level

@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = "Waila")
class SixNodeWailaProvider : IWailaDataProvider {
    private fun getSixData(accessor: IWailaDataAccessor): SixNodeWailaData? {
        val coord = Coordinate(accessor.position.blockX, accessor.position.blockY, accessor.position.blockZ,
            accessor.world)
        val side = Direction.from(accessor.side)
        var sixData: SixNodeWailaData? = null
        try {
            sixData = WailaCache.sixNodes.get(SixNodeCoordonate(coord, side))
        } catch(e: CacheLoader.InvalidCacheLoadException) {
        }

        return sixData
    }

    override fun getWailaBody(itemStack: ItemStack?, currenttip: MutableList<String>, accessor: IWailaDataAccessor,
                              config: IWailaConfigHandler?): MutableList<String> {
        getSixData(accessor)?.data?.forEach {
            currenttip.add("${it.key}: ${SpecialChars.WHITE}${it.value}")
        }

        return currenttip
    }

    override fun getWailaStack(accessor: IWailaDataAccessor, config: IWailaConfigHandler?): ItemStack?
        = getSixData(accessor)?.itemStack

    override fun getWailaTail(itemStack: ItemStack?, currenttip: MutableList<String>, accessor: IWailaDataAccessor?,
                              config: IWailaConfigHandler?): MutableList<String> = currenttip

    override fun getNBTData(player: ServerPlayer?, te: TileEntity?, tag: CompoundTag?, world: World?,
                            x: Int, y: Int, z: Int): CompoundTag? = null

    override fun getWailaHead(itemStack: ItemStack?, currenttip: MutableList<String>, accessor: IWailaDataAccessor,
                              config: IWailaConfigHandler?): MutableList<String> = if (itemStack != null) {
        mutableListOf("${SpecialChars.WHITE}${itemStack.displayName}")
    } else {
        currenttip
    }
}

package mods.eln.misc

import net.minecraft.tileentity.TileEntity
import java.util.*

class TileEntityDestructor {
    var destroyList = ArrayList<TileEntity>()
    fun clear() {
        destroyList.clear()
    }

    fun add(tile: TileEntity) {
        destroyList.add(tile)
    }

    @SubscribeEvent
    fun tick(event: ServerTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        for (t in destroyList) {
            if (t.level != null && t.level.getTileEntity(t.xCoord, t.yCoord, t.zCoord) === t) {
                t.level.setBlockToAir(t.xCoord, t.yCoord, t.zCoord)
                Utils.println("destroy light at " + t.xCoord + " " + t.yCoord + " " + t.zCoord)
            }
        }
        destroyList.clear()
    }

    init {
        FMLCommonHandler.instance().bus().register(this)
    }
}

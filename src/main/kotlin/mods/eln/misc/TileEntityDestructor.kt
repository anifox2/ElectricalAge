package mods.eln.misc

import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.common.MinecraftForge
import java.util.*

class TileEntityDestructor {
    var destroyList = ArrayList<BlockEntity>()
    fun clear() {
        destroyList.clear()
    }

    fun add(tile: BlockEntity) {
        destroyList.add(tile)
    }

    @SubscribeEvent
    fun tick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        for (t in destroyList) {
            if (t.level != null && t.level!!.getBlockEntity(t.blockPos) === t) {
                t.level!!.removeBlock(t.blockPos, false)
                Utils.println("destroy light at " + t.blockPos.x + " " + t.blockPos.y + " " + t.blockPos.z)
            }
        }
        destroyList.clear()
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }
}

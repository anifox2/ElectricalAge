package mods.eln.server

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*

class DelayedTaskManager {
    var tasks = LinkedList<ITask>()
    fun clear() {
        tasks.clear()
    }

    @SubscribeEvent
    fun tick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        val cpy: List<ITask> = ArrayList(tasks)
        tasks.clear()
        for (t in cpy) {
            t.run()
        }
    }

    interface ITask {
        fun run()
    }

    fun add(t: ITask) {
        tasks.add(t)
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }
}

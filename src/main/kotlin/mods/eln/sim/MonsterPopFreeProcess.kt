package mods.eln.sim

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.misc.Utils
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import java.util.ArrayList

class MonsterPopFreeProcess(val level: Level, val coordinate: Coordinate, val range: Int) : IProcess {

    var timerCounter = 0.0
    val timerPeriod = 0.212

    var oldList: List<*>? = null

    override fun process(time: Double) {
        //Monster killing must be active before continuing :
        if (!Eln.killMonstersAroundLamps)
            return

        timerCounter += time
        if (timerCounter > timerPeriod) {
            timerCounter -= Utils.rand(1.0, 1.5) * timerPeriod
            
            val aabb = AABB(
                (coordinate.x - range).toDouble(), (coordinate.y - range).toDouble(), (coordinate.z - range).toDouble(),
                (coordinate.x + range + 1).toDouble(), (coordinate.y + range + 1).toDouble(), (coordinate.z + range + 1).toDouble()
            )
            
            val list = level.getEntitiesOfClass(LivingEntity::class.java, aabb) { it is Enemy }

            for (o in list) {
                val mob = o as Enemy
                if (oldList == null || !oldList!!.contains(o)) {
                    // Distance check might be needed if AABB is rough
                    // coordinate.distanceTo(mob) < range
                    // For now, AABB is close enough
                    
                    // Check exclusions
                    // ReplicatorEntity? Not available in context yet
                    if (o !is WitherBoss && o !is EnderMan) {
                        if (o is net.minecraft.world.entity.LivingEntity) {
                            o.kill()
                            Utils.println("MonsterPopFreeProcess : Dead")
                        }
                    }
                }
            }
            oldList = list
        }
    }
}

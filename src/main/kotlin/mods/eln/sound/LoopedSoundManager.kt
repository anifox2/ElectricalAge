package mods.eln.sound

import mods.eln.Eln
import net.minecraft.client.Minecraft
import net.minecraft.client.sounds.SoundManager

class LoopedSoundManager(val updateInterval: Float = 0.5f) {
    private var remaining = 0f
    private val loops = mutableSetOf<LoopedSound>()

    fun add(loop: LoopedSound?) {
        if (loop != null && loop.active) {
            loops.add(loop)
        }
    }

    fun dispose() = loops.forEach { it.active = false }

    // takes in two points and gets the squared distance delta between them
    fun sqDistDelta(cx: Double, cy: Double, cz: Double, px: Double, py: Double, pz: Double) = (cx - px) * (cx - px) + (cy - py) * (cy - py) + (cz - pz) * (cz - pz)

    fun process(deltaT: Float) {
        remaining -= deltaT
        if (remaining <= 0) {
            val soundManager = Minecraft.getInstance().soundManager
            loops.forEach {
                // add 0.5 to put the point in the center of the block making sounds
                val cx = it.coord.x + 0.5
                val cy = it.coord.y + 0.5
                val cz = it.coord.z + 0.5
                // get the player, and get the squared distance between the player and the block
                val player = Minecraft.getInstance().player ?: return@forEach
                val distDeltaSquared = sqDistDelta(cx, cy, cz, player.getX(), player.getY(), player.getZ())
                // when comparing, compare distDeltaSquared to the square of the distance delta that you are trying to compare against.
                if (it.volume > 0 && it.pitch > 0 && !soundManager.isActive(it) && distDeltaSquared < Eln.maxSoundDistance * Eln.maxSoundDistance) {
                    try {
                        soundManager.play(it)
                    } catch (e: IllegalArgumentException) {
                        System.out.println(e)
                    }
                }
                if (distDeltaSquared >= Eln.maxSoundDistance * Eln.maxSoundDistance || it.volume == 0f || it.pitch == 0f) {
                    try {
                        soundManager.stop(it)
                    }catch (e: Exception) {
                        System.out.println(e)
                    }
                }
            }
            remaining = updateInterval
        }
    }
}

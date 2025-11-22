package mods.eln.sound

import mods.eln.client.UuidManager
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent

class SoundClientEventListener(var uuidManager: UuidManager) {
    @JvmField
    var currentUuid: ArrayList<Int>? = null

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun event(e: PlaySoundEvent) {
        if (currentUuid == null) return
        val sound = e.sound ?: return
        val soundManager = Minecraft.getInstance().soundManager
        uuidManager.add(currentUuid!!, SoundClientEntity(soundManager, sound))
    }

    internal class KillSound {
        var sound: SoundInstance? = null
        var sm: SoundManager? = null

        fun kill() {
            sm!!.stop(sound)
        }
    }
}

package mods.eln.sound

import mods.eln.client.ClientProxy
import mods.eln.client.SoundLoader
import mods.eln.misc.Utils.TraceRayWeightOpaque
import mods.eln.misc.Utils.println
import mods.eln.misc.Utils.traceRay
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.minecraftforge.registries.ForgeRegistries
import kotlin.math.pow
import kotlin.math.sqrt

object SoundClient {
    fun play(p: SoundCommand) {
        ClientProxy.soundClientEventListener.currentUuid = p.uuid

        val player: Player? = Minecraft.getInstance().player
        if (player == null) return
        if (p.world!!.dimension() != player.level().dimension()) return
        
        val px = player.getX()
        val py = player.getY()
        val pz = player.getZ()
        
        val distance = sqrt((p.x - px).pow(2.0) + (p.y - py).pow(2.0) + (p.z - pz).pow(2.0))
        if (distance >= p.rangeMax) return
        
        var distanceFactor = 1f
        if (distance > p.rangeNominal) {
            distanceFactor = ((p.rangeMax - distance) / (p.rangeMax - p.rangeNominal)).toFloat()
        }

        val blockFactor = traceRay(
            p.world!!,
            px,
            py,
            pz,
            p.x,
            p.y,
            p.z,
            TraceRayWeightOpaque()
        ) * p.blockFactor

        val trackCount = SoundLoader.getTrackCount(p.track!!)

        if (trackCount == 1) {
            val temp = 1.0f / (1 + blockFactor)
            p.volume *= temp.pow(2.0f)
            p.volume *= distanceFactor
            if (p.volume <= 0) return

            val soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation(p.track!!))
            if (soundEvent != null) {
                p.world!!.playLocalSound(
                    px + 2.0 * (p.x - px) / distance,
                    py + 2.0 * (p.y - py) / distance,
                    pz + 2.0 * (p.z - pz) / distance,
                    soundEvent,
                    SoundSource.BLOCKS,
                    p.volume,
                    p.pitch,
                    false
                )
            }
        } else {
            for (idx in 0 until trackCount) {
                var bandVolume = p.volume
                bandVolume *= distanceFactor

                bandVolume -= (((trackCount - 1 - idx) / (trackCount - 1f) + 0.2) * blockFactor).toFloat()
                println(bandVolume)
                
                val soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation(p.track + "_" + idx + "x"))
                if (soundEvent != null) {
                    p.world!!.playLocalSound(
                        px + 2.0 * (p.x - px) / distance,
                        py + 2.0 * (p.y - py) / distance,
                        pz + 2.0 * (p.z - pz) / distance,
                        soundEvent,
                        SoundSource.BLOCKS,
                        bandVolume,
                        p.pitch,
                        false
                    )
                }
            }
        }

        ClientProxy.soundClientEventListener.currentUuid = null
    }
}

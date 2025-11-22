package mods.eln.sound

import mods.eln.misc.Coordinate
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.resources.sounds.TickableSoundInstance
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.RandomSource

abstract class LoopedSound(val sample: String, val coord: Coordinate,
                           val attenuationType: SoundInstance.Attenuation = SoundInstance.Attenuation.LINEAR) 
    : AbstractTickableSoundInstance(SoundEvent.createVariableRangeEvent(ResourceLocation(sample)), SoundSource.BLOCKS, RandomSource.create()) {

    var active = true

    init {
        this.x = coord.x.toDouble() + 0.5
        this.y = coord.y.toDouble() + 0.5
        this.z = coord.z.toDouble() + 0.5
        this.looping = true
        this.attenuation = attenuationType
    }

    override fun tick() {
        if (!active) {
            stop()
        }
        // Sync fields with abstract methods if needed, but since we override getVolume/getPitch, it should be fine.
        // However, AbstractTickableSoundInstance uses fields for volume/pitch in its implementation of getVolume/getPitch.
        // If we override them, we are good.
    }
    
    // We keep these abstract/open as in the original class so subclasses can override them.
    // But AbstractTickableSoundInstance has them as final? No, they are open in SoundInstance.
    // But AbstractTickableSoundInstance overrides them to return the field.
    
    override fun getVolume(): Float = 1f
    override fun getPitch(): Float = 1f
}

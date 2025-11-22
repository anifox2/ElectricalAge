package mods.eln.client

import mods.eln.CommonProxy
import mods.eln.sound.SoundClientEventListener

object ClientProxy : CommonProxy() {
    lateinit var uuidManager: UuidManager
    lateinit var soundClientEventListener: SoundClientEventListener

    override fun registerRenderers() {
        uuidManager = UuidManager()
        soundClientEventListener = SoundClientEventListener(uuidManager)
    }
}

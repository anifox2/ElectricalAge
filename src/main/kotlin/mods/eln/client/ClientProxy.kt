package mods.eln.client

import mods.eln.CommonProxy
import mods.eln.sound.SoundClientEventListener
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

object ClientProxy : CommonProxy() {
    lateinit var uuidManager: UuidManager
    lateinit var soundClientEventListener: SoundClientEventListener
    val clientKeyHandler = ClientKeyHandler()

    override fun registerRenderers() {
        uuidManager = UuidManager()
        soundClientEventListener = SoundClientEventListener(uuidManager)
        
        MinecraftForge.EVENT_BUS.register(clientKeyHandler)
        FMLJavaModLoadingContext.get().modEventBus.addListener(clientKeyHandler::registerBindings)
    }
}

package mods.eln.client

import mods.eln.CommonProxy
import mods.eln.sound.SoundClientEventListener
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import mods.eln.init.Registration
import mods.eln.node.six.SixNodeRender
import mods.eln.node.transparent.TransparentNodeRender
import net.minecraftforge.client.event.EntityRenderersEvent

object ClientProxy : CommonProxy() {
    lateinit var uuidManager: UuidManager
    lateinit var soundClientEventListener: SoundClientEventListener
    val clientKeyHandler = ClientKeyHandler()

    override fun registerRenderers() {
        uuidManager = UuidManager()
        soundClientEventListener = SoundClientEventListener(uuidManager)
        
        MinecraftForge.EVENT_BUS.register(clientKeyHandler)
        FMLJavaModLoadingContext.get().modEventBus.addListener(clientKeyHandler::registerBindings)
        FMLJavaModLoadingContext.get().modEventBus.addListener(this::registerBlockEntityRenderers)
    }

    fun registerBlockEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(Registration.SIX_NODE_BLOCK_ENTITY.get(), ::SixNodeRender)
        event.registerBlockEntityRenderer(Registration.TRANSPARENT_NODE_BLOCK_ENTITY.get(), ::TransparentNodeRender)
    }
}

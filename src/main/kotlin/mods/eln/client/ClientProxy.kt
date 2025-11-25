package mods.eln.client

import mods.eln.CommonProxy
import mods.eln.sound.SoundClientEventListener
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import mods.eln.init.Registration
import mods.eln.node.six.SixNodeRender
import mods.eln.node.transparent.TransparentNodeRender
import net.minecraftforge.client.event.EntityRenderersEvent
import mods.eln.transparentnode.FuelHeatFurnaceGui
import net.minecraft.client.gui.screens.MenuScreens

object ClientProxy : CommonProxy() {
    lateinit var uuidManager: UuidManager
    lateinit var soundClientEventListener: SoundClientEventListener
    val clientKeyHandler = ClientKeyHandler()

    fun setup(modEventBus: net.minecraftforge.eventbus.api.IEventBus) {
        modEventBus.addListener(this::registerBlockEntityRenderers)
        modEventBus.addListener(clientKeyHandler::registerBindings)
        modEventBus.addListener(this::clientSetup)
    }

    fun clientSetup(event: net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent) {
        event.enqueueWork {
            MenuScreens.register(Registration.FUEL_HEAT_FURNACE_MENU.get()) { menu, inv, title -> FuelHeatFurnaceGui(menu, inv, title) }
            MenuScreens.register(Registration.HEAT_FURNACE_MENU.get()) { menu, inv, title -> mods.eln.transparentnode.heatfurnace.HeatFurnaceGui(menu, inv, title) }
        }
    }

    override fun registerRenderers() {
        uuidManager = UuidManager()
        soundClientEventListener = SoundClientEventListener(uuidManager)
        
        MinecraftForge.EVENT_BUS.register(clientKeyHandler)
    }

    fun registerBlockEntityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(Registration.SIX_NODE_BLOCK_ENTITY.get(), ::SixNodeRender)
        event.registerBlockEntityRenderer(Registration.TRANSPARENT_NODE_BLOCK_ENTITY.get(), ::TransparentNodeRender)
    }
}

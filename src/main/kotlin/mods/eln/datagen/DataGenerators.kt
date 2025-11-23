package mods.eln.datagen

import mods.eln.Eln
import net.minecraft.data.DataGenerator
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = Eln.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
object DataGenerators {
    @SubscribeEvent
    fun gatherData(event: GatherDataEvent) {
        val generator = event.generator
        val packOutput = generator.packOutput
        val existingFileHelper = event.existingFileHelper
        val lookupProvider = event.lookupProvider

        generator.addProvider(event.includeServer(), ElnRecipeProvider(packOutput))
    }
}

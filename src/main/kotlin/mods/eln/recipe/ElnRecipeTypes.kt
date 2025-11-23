package mods.eln.recipe

import mods.eln.Eln
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object ElnRecipeTypes {
    val RECIPE_SERIALIZERS: DeferredRegister<RecipeSerializer<*>> = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Eln.MODID)
    val RECIPE_TYPES: DeferredRegister<RecipeType<*>> = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Eln.MODID)

    val MACERATOR_SERIALIZER: RegistryObject<MaceratorRecipe.Serializer> = RECIPE_SERIALIZERS.register("macerator") { MaceratorRecipe.Serializer.INSTANCE }
    val MACERATOR_TYPE: RegistryObject<MaceratorRecipe.Type> = RECIPE_TYPES.register("macerator") { MaceratorRecipe.Type.INSTANCE }

    val ARC_FURNACE_SERIALIZER: RegistryObject<ArcFurnaceRecipe.Serializer> = RECIPE_SERIALIZERS.register("arc_furnace") { ArcFurnaceRecipe.Serializer.INSTANCE }
    val ARC_FURNACE_TYPE: RegistryObject<ArcFurnaceRecipe.Type> = RECIPE_TYPES.register("arc_furnace") { ArcFurnaceRecipe.Type.INSTANCE }

    val PLATE_MACHINE_SERIALIZER: RegistryObject<PlateMachineRecipe.Serializer> = RECIPE_SERIALIZERS.register("plate_machine") { PlateMachineRecipe.Serializer.INSTANCE }
    val PLATE_MACHINE_TYPE: RegistryObject<PlateMachineRecipe.Type> = RECIPE_TYPES.register("plate_machine") { PlateMachineRecipe.Type.INSTANCE }

    val COMPRESSOR_SERIALIZER: RegistryObject<CompressorRecipe.Serializer> = RECIPE_SERIALIZERS.register("compressor") { CompressorRecipe.Serializer.INSTANCE }
    val COMPRESSOR_TYPE: RegistryObject<CompressorRecipe.Type> = RECIPE_TYPES.register("compressor") { CompressorRecipe.Type.INSTANCE }

    val MAGNETIZER_SERIALIZER: RegistryObject<MagnetizerRecipe.Serializer> = RECIPE_SERIALIZERS.register("magnetizer") { MagnetizerRecipe.Serializer.INSTANCE }
    val MAGNETIZER_TYPE: RegistryObject<MagnetizerRecipe.Type> = RECIPE_TYPES.register("magnetizer") { MagnetizerRecipe.Type.INSTANCE }

    fun register(eventBus: IEventBus) {
        RECIPE_SERIALIZERS.register(eventBus)
        RECIPE_TYPES.register(eventBus)
    }
}

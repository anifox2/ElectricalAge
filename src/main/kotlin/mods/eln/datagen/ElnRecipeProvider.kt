package mods.eln.datagen

import mods.eln.Eln
import mods.eln.recipe.ElnRecipeTypes
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Blocks
import java.util.function.Consumer
import com.google.gson.JsonObject
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.ItemStack

class ElnRecipeProvider(packOutput: PackOutput) : RecipeProvider(packOutput) {
    override fun buildRecipes(pWriter: Consumer<FinishedRecipe>) {
        // Example Macerator Recipe
        macerator(pWriter, Ingredient.of(Blocks.COAL_ORE), ItemStack(Items.COAL, 3), 4000.0)
        
        // Example Arc Furnace Recipe
        arcFurnace(pWriter, Ingredient.of(Blocks.IRON_ORE), ItemStack(Items.IRON_INGOT, 2), 200000.0)
        
        // TODO: Migrate recipes from CraftingRecipes.kt

        // Macerator Recipes
        val maceratorEnergy = 4000.0
        
        // Coal Ore -> 3 Coal
        macerator(pWriter, Ingredient.of(Blocks.COAL_ORE), ItemStack(Items.COAL, 3), maceratorEnergy)
        
        // Copper Ore -> 2 Copper Dust
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Copper Dust")?.let { dust ->
             macerator(pWriter, Ingredient.of(net.minecraftforge.common.Tags.Items.ORES_COPPER), dust.newItemStack(2), maceratorEnergy)
        }
        
        // Iron Ore -> 2 Iron Dust
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Iron Dust")?.let { dust ->
             macerator(pWriter, Ingredient.of(net.minecraftforge.common.Tags.Items.ORES_IRON), dust.newItemStack(2), maceratorEnergy)
        }
        
        // Gold Ore -> 2 Gold Dust
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Gold Dust")?.let { dust ->
             macerator(pWriter, Ingredient.of(net.minecraftforge.common.Tags.Items.ORES_GOLD), dust.newItemStack(2), maceratorEnergy)
        }
        
        // Lead Ore -> 2 Lead Dust
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Lead Dust")?.let { dust ->
             // Assuming forge:ores/lead tag exists
             // macerator(pWriter, Ingredient.of(net.minecraftforge.common.Tags.Items.ORES_LEAD), dust.newItemStack(2), maceratorEnergy)
             // Forge tags might need to be accessed via ItemTags.create
        }
        
        // Tungsten Ore -> 2 Tungsten Dust
        
        // Coal -> Coal Dust
        
        // Charcoal -> Coal Dust (Maybe?)
        
        // Arc Furnace Recipes
        val arcFurnaceEnergy = 5000.0
        
        // Iron Ore -> 2 Iron Ingot
        arcFurnace(pWriter, Ingredient.of(Blocks.IRON_ORE), ItemStack(Items.IRON_INGOT, 2), arcFurnaceEnergy)
        
        // Gold Ore -> 2 Gold Ingot
        arcFurnace(pWriter, Ingredient.of(Blocks.GOLD_ORE), ItemStack(Items.GOLD_INGOT, 2), arcFurnaceEnergy)
        
        // Coal Ore -> 2 Coal
        arcFurnace(pWriter, Ingredient.of(Blocks.COAL_ORE), ItemStack(Items.COAL, 2), arcFurnaceEnergy)
        
        // Redstone Ore -> 6 Redstone
        arcFurnace(pWriter, Ingredient.of(Blocks.REDSTONE_ORE), ItemStack(Items.REDSTONE, 6), arcFurnaceEnergy)
        
        // Lapis Ore -> 1 Lapis Block
        arcFurnace(pWriter, Ingredient.of(Blocks.LAPIS_ORE), ItemStack(Blocks.LAPIS_BLOCK, 1), arcFurnaceEnergy)
        
        // Diamond Ore -> 2 Diamond
        arcFurnace(pWriter, Ingredient.of(Blocks.DIAMOND_ORE), ItemStack(Items.DIAMOND, 2), arcFurnaceEnergy)
        
        // Emerald Ore -> 2 Emerald
        arcFurnace(pWriter, Ingredient.of(Blocks.EMERALD_ORE), ItemStack(Items.EMERALD, 2), arcFurnaceEnergy)

        // Plate Machine Recipes
        val plateEnergy = 10000.0
        
        // Copper Ingot -> Copper Plate
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Copper Plate")?.let { plate ->
             plateMachine(pWriter, Ingredient.of(net.minecraftforge.common.Tags.Items.INGOTS_COPPER), plate.newItemStack(), plateEnergy)
        }
        
        // Lead Ingot -> Lead Plate
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Lead Plate")?.let { plate ->
             mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Lead Ingot")?.let { ingot ->
                 plateMachine(pWriter, Ingredient.of(ingot.newItemStack()), plate.newItemStack(), plateEnergy)
             }
        }
        
        // Silicon Ingot -> Silicon Plate
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Silicon Plate")?.let { output ->
             mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Silicon Ingot")?.let { input ->
                 plateMachine(pWriter, Ingredient.of(input.newItemStack()), output.newItemStack(), plateEnergy)
             }
        }
        
        // Alloy Ingot -> Alloy Plate
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Alloy Plate")?.let { output ->
             mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Alloy Ingot")?.let { input ->
                 plateMachine(pWriter, Ingredient.of(input.newItemStack()), output.newItemStack(), plateEnergy)
             }
        }
        
        // Iron Ingot -> Iron Plate
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Iron Plate")?.let { output ->
             plateMachine(pWriter, Ingredient.of(Items.IRON_INGOT), output.newItemStack(), plateEnergy)
        }
        
        // Gold Ingot -> Gold Plate
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Gold Plate")?.let { output ->
             plateMachine(pWriter, Ingredient.of(Items.GOLD_INGOT), output.newItemStack(), plateEnergy)
        }

        // Compressor Recipes
        
        // Graphite Rod -> Synthetic Diamond
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Synthetic Diamond")?.let { output ->
             mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Graphite Rod")?.let { input ->
                 compressor(pWriter, Ingredient.of(input.newItemStack()), output.newItemStack(), 80000.0)
             }
        }
        
        // Coal Dust -> Coal Plate
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Coal Plate")?.let { output ->
             mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Coal Dust")?.let { input ->
                 compressor(pWriter, Ingredient.of(input.newItemStack()), output.newItemStack(), 40000.0)
             }
        }
        
        // Coal Plate -> Graphite Rod
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Graphite Rod")?.let { output ->
             mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Coal Plate")?.let { input ->
                 compressor(pWriter, Ingredient.of(input.newItemStack()), output.newItemStack(), 80000.0)
             }
        }
        
        // Sand -> Dielectric
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Dielectric")?.let { output ->
             compressor(pWriter, Ingredient.of(net.minecraftforge.common.Tags.Items.SAND), output.newItemStack(), 2000.0)
        }
        
        // Log -> Tree Resin
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Tree Resin")?.let { output ->
             compressor(pWriter, Ingredient.of(net.minecraft.tags.ItemTags.LOGS), output.newItemStack(), 3000.0)
        }
        
        // Magnetizer Recipes
        
        // Iron Ingot -> Basic Magnet
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Basic Magnet")?.let { output ->
             magnetizer(pWriter, Ingredient.of(Items.IRON_INGOT), output.newItemStack(), 5000.0)
        }
        
        // Alloy Ingot -> Advanced Magnet
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Advanced Magnet")?.let { output ->
             mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Alloy Ingot")?.let { input ->
                 magnetizer(pWriter, Ingredient.of(input.newItemStack()), output.newItemStack(), 15000.0)
             }
        }
        
        // Copper Dust -> Redstone
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Copper Dust")?.let { input ->
             magnetizer(pWriter, Ingredient.of(input.newItemStack()), ItemStack(Items.REDSTONE), 5000.0)
        }
        
        // Basic Magnet -> Optimal Ferromagnetic Core
        mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Optimal Ferromagnetic Core")?.let { output ->
             mods.eln.generic.GenericItemUsingDamageDescriptor.getByName("Basic Magnet")?.let { input ->
                 magnetizer(pWriter, Ingredient.of(input.newItemStack()), output.newItemStack(), 5000.0)
             }
        }
    }

    fun macerator(consumer: Consumer<FinishedRecipe>, input: Ingredient, output: ItemStack, energy: Double) {
        // Generate a unique ID based on input and output
        val inputId = if (input.items.isNotEmpty()) net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(input.items[0].item).toString().replace(":", "_") else "unknown"
        val outputId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(output.item).toString().replace(":", "_")
        val id = ResourceLocation(Eln.MODID, "macerator/${outputId}_from_${inputId}")
        
        consumer.accept(object : FinishedRecipe {
            override fun serializeRecipeData(pJson: JsonObject) {
                pJson.add("input", input.toJson())
                pJson.add("output", serializeItemStack(output))
                pJson.addProperty("energy", energy)
            }

            override fun getId(): ResourceLocation {
                return id
            }

            override fun getType(): RecipeSerializer<*> {
                return ElnRecipeTypes.MACERATOR_SERIALIZER.get()
            }

            override fun serializeAdvancement(): JsonObject? {
                return null
            }

            override fun getAdvancementId(): ResourceLocation? {
                return null
            }
        })
    }
    
    fun arcFurnace(consumer: Consumer<FinishedRecipe>, input: Ingredient, output: ItemStack, energy: Double) {
        val inputId = if (input.items.isNotEmpty()) net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(input.items[0].item).toString().replace(":", "_") else "unknown"
        val outputId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(output.item).toString().replace(":", "_")
        val id = ResourceLocation(Eln.MODID, "arc_furnace/${outputId}_from_${inputId}")
        
        consumer.accept(object : FinishedRecipe {
            override fun serializeRecipeData(pJson: JsonObject) {
                pJson.add("input", input.toJson())
                pJson.add("output", serializeItemStack(output))
                pJson.addProperty("energy", energy)
            }

            override fun getId(): ResourceLocation {
                return id
            }

            override fun getType(): RecipeSerializer<*> {
                return ElnRecipeTypes.ARC_FURNACE_SERIALIZER.get()
            }

            override fun serializeAdvancement(): JsonObject? {
                return null
            }

            override fun getAdvancementId(): ResourceLocation? {
                return null
            }
        })
    }
    
    fun plateMachine(consumer: Consumer<FinishedRecipe>, input: Ingredient, output: ItemStack, energy: Double) {
        val inputId = if (input.items.isNotEmpty()) net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(input.items[0].item).toString().replace(":", "_") else "unknown"
        val outputId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(output.item).toString().replace(":", "_")
        val id = ResourceLocation(Eln.MODID, "plate_machine/${outputId}_from_${inputId}")
        
        consumer.accept(object : FinishedRecipe {
            override fun serializeRecipeData(pJson: JsonObject) {
                pJson.add("input", input.toJson())
                pJson.add("output", serializeItemStack(output))
                pJson.addProperty("energy", energy)
            }

            override fun getId(): ResourceLocation {
                return id
            }

            override fun getType(): RecipeSerializer<*> {
                return ElnRecipeTypes.PLATE_MACHINE_SERIALIZER.get()
            }

            override fun serializeAdvancement(): JsonObject? {
                return null
            }

            override fun getAdvancementId(): ResourceLocation? {
                return null
            }
        })
    }

    fun compressor(consumer: Consumer<FinishedRecipe>, input: Ingredient, output: ItemStack, energy: Double) {
        val inputId = if (input.items.isNotEmpty()) net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(input.items[0].item).toString().replace(":", "_") else "unknown"
        val outputId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(output.item).toString().replace(":", "_")
        val id = ResourceLocation(Eln.MODID, "compressor/${outputId}_from_${inputId}")
        
        consumer.accept(object : FinishedRecipe {
            override fun serializeRecipeData(pJson: JsonObject) {
                pJson.add("input", input.toJson())
                pJson.add("output", serializeItemStack(output))
                pJson.addProperty("energy", energy)
            }

            override fun getId(): ResourceLocation {
                return id
            }

            override fun getType(): RecipeSerializer<*> {
                return ElnRecipeTypes.COMPRESSOR_SERIALIZER.get()
            }

            override fun serializeAdvancement(): JsonObject? {
                return null
            }

            override fun getAdvancementId(): ResourceLocation? {
                return null
            }
        })
    }

    fun magnetizer(consumer: Consumer<FinishedRecipe>, input: Ingredient, output: ItemStack, energy: Double) {
        val inputId = if (input.items.isNotEmpty()) net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(input.items[0].item).toString().replace(":", "_") else "unknown"
        val outputId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(output.item).toString().replace(":", "_")
        val id = ResourceLocation(Eln.MODID, "magnetizer/${outputId}_from_${inputId}")
        
        consumer.accept(object : FinishedRecipe {
            override fun serializeRecipeData(pJson: JsonObject) {
                pJson.add("input", input.toJson())
                pJson.add("output", serializeItemStack(output))
                pJson.addProperty("energy", energy)
            }

            override fun getId(): ResourceLocation {
                return id
            }

            override fun getType(): RecipeSerializer<*> {
                return ElnRecipeTypes.MAGNETIZER_SERIALIZER.get()
            }

            override fun serializeAdvancement(): JsonObject? {
                return null
            }

            override fun getAdvancementId(): ResourceLocation? {
                return null
            }
        })
    }
    
    private fun serializeItemStack(stack: ItemStack): JsonObject {
        val json = JsonObject()
        json.addProperty("item", net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.item).toString())
        if (stack.count > 1) {
            json.addProperty("count", stack.count)
        }
        return json
    }
}

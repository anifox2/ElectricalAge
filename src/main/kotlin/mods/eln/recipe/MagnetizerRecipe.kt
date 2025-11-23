package mods.eln.recipe

import com.google.gson.JsonObject
import mods.eln.Eln
import net.minecraft.core.NonNullList
import net.minecraft.core.RegistryAccess
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

class MagnetizerRecipe(
    private val id: ResourceLocation,
    val input: Ingredient,
    val outputs: NonNullList<ItemStack>,
    val energy: Double
) : Recipe<Container> {

    override fun matches(pContainer: Container, pLevel: Level): Boolean {
        return input.test(pContainer.getItem(0))
    }

    override fun assemble(pContainer: Container, pRegistryAccess: RegistryAccess): ItemStack {
        return outputs[0].copy()
    }

    override fun canCraftInDimensions(pWidth: Int, pHeight: Int): Boolean {
        return true
    }

    override fun getResultItem(pRegistryAccess: RegistryAccess): ItemStack {
        return outputs[0]
    }

    override fun getId(): ResourceLocation {
        return id
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return Serializer.INSTANCE
    }

    override fun getType(): RecipeType<*> {
        return Type.INSTANCE
    }

    class Type : RecipeType<MagnetizerRecipe> {
        override fun toString(): String {
            return Eln.MODID + ":magnetizer"
        }

        companion object {
            val INSTANCE = Type()
            val ID = ResourceLocation(Eln.MODID, "magnetizer")
        }
    }

    class Serializer : RecipeSerializer<MagnetizerRecipe> {
        override fun fromJson(pRecipeId: ResourceLocation, pSerializedRecipe: JsonObject): MagnetizerRecipe {
            val input = Ingredient.fromJson(pSerializedRecipe.get("input"))
            val outputs = NonNullList.create<ItemStack>()
            
            if (pSerializedRecipe.has("outputs")) {
                val outputArray = GsonHelper.getAsJsonArray(pSerializedRecipe, "outputs")
                outputArray.forEach { 
                    outputs.add(ShapedRecipe.itemStackFromJson(it.asJsonObject))
                }
            } else if (pSerializedRecipe.has("output")) {
                 outputs.add(ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output")))
            }
            
            val energy = GsonHelper.getAsDouble(pSerializedRecipe, "energy", 0.0)

            return MagnetizerRecipe(pRecipeId, input, outputs, energy)
        }

        override fun fromNetwork(pRecipeId: ResourceLocation, pBuffer: FriendlyByteBuf): MagnetizerRecipe? {
            val input = Ingredient.fromNetwork(pBuffer)
            val outputCount = pBuffer.readInt()
            val outputs = NonNullList.create<ItemStack>()
            for (i in 0 until outputCount) {
                outputs.add(pBuffer.readItem())
            }
            val energy = pBuffer.readDouble()
            return MagnetizerRecipe(pRecipeId, input, outputs, energy)
        }

        override fun toNetwork(pBuffer: FriendlyByteBuf, pRecipe: MagnetizerRecipe) {
            pRecipe.input.toNetwork(pBuffer)
            pBuffer.writeInt(pRecipe.outputs.size)
            for (output in pRecipe.outputs) {
                pBuffer.writeItem(output)
            }
            pBuffer.writeDouble(pRecipe.energy)
        }

        companion object {
            val INSTANCE = Serializer()
            val ID = ResourceLocation(Eln.MODID, "magnetizer")
        }
    }
}

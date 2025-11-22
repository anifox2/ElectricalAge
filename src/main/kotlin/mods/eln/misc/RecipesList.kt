package mods.eln.misc

import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.collections.ArrayList

class RecipesList {
    val recipes = ArrayList<Recipe>()
    val machines = ArrayList<ItemStack>()
    fun addRecipe(recipe: Recipe) {
        recipes.add(recipe)
        recipe.setMachineList(machines)
    }

    fun addMachine(machine: ItemStack) {
        machines.add(machine)
    }
    
    fun getRecipe(input: ItemStack): Recipe? {
        for (r in recipes) {
            if (ItemStack.isSameItem(r.input, input)) return r
        }
        return null
    }
}

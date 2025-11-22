package mods.eln.misc

import net.minecraft.world.item.ItemStack
import java.util.*

class Recipe {
    @JvmField
    var input: ItemStack
    @JvmField
    var output: Array<ItemStack>
    @JvmField
    var energy: Double

    constructor(input: ItemStack, output: Array<ItemStack>, energy: Double) {
        this.input = input
        this.output = output
        this.energy = energy
    }

    constructor(input: ItemStack, output: ItemStack, energy: Double) {
        this.input = input
        this.output = arrayOf(output)
        this.energy = energy
    }
    
    fun setMachineList(machines: ArrayList<ItemStack>) {
        // Stub
    }

    fun getOutputCopy(): Array<ItemStack> {
        return output.map { it.copy() }.toTypedArray()
    }
}

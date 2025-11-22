package mods.eln.wiki

import net.minecraft.world.item.ItemStack
import java.util.ArrayList
import java.util.HashMap
import mods.eln.i18n.I18N.tr

object Data {
    val groupes = HashMap<String, ArrayList<ItemStack>>()

    fun add(str: String, stack: ItemStack) {
        var groupe = groupes[str]
        if (groupe == null) {
            groupe = ArrayList<ItemStack>()
            groupes[str] = groupe
        }
        groupe.add(stack)
    }

    fun addLight(stack: ItemStack) {
        add(tr("Light"), stack)
    }

    fun addMachine(stack: ItemStack) {
        add(tr("Machine"), stack)
    }

    fun addWiring(stack: ItemStack) {
        add(tr("Wiring"), stack)
    }

    fun addThermal(stack: ItemStack) {
        add(tr("Thermal"), stack)
    }

    fun addEnergy(stack: ItemStack) {
        add(tr("Energy"), stack)
    }

    fun addUtilities(stack: ItemStack) {
        add(tr("Utilities"), stack)
    }

    fun addSignal(stack: ItemStack) {
        add(tr("Signal"), stack)
    }

    fun addResource(stack: ItemStack) {
        add("Resource", stack)
    }
}

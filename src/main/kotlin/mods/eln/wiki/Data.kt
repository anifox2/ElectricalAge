package mods.eln.wiki

import net.minecraft.world.item.ItemStack
import java.util.ArrayList
import java.util.HashMap
import mods.eln.i18n.I18N.tr

object Data {
    val groupes = HashMap<String, ArrayList<ItemStack>>()

    @JvmStatic
    fun add(str: String, stack: ItemStack) {
        var groupe = groupes[str]
        if (groupe == null) {
            groupe = ArrayList<ItemStack>()
            groupes[str] = groupe
        }
        groupe.add(stack)
    }

    @JvmStatic
    fun addLight(stack: ItemStack) {
        add(tr("Light"), stack)
    }

    @JvmStatic
    fun addMachine(stack: ItemStack) {
        add(tr("Machine"), stack)
    }

    @JvmStatic
    fun addWiring(stack: ItemStack) {
        add(tr("Wiring"), stack)
    }

    @JvmStatic
    fun addThermal(stack: ItemStack) {
        add(tr("Thermal"), stack)
    }

    @JvmStatic
    fun addEnergy(stack: ItemStack) {
        add(tr("Energy"), stack)
    }

    @JvmStatic
    fun addUtilities(stack: ItemStack) {
        add(tr("Utilities"), stack)
    }

    @JvmStatic
    fun addSignal(stack: ItemStack) {
        add(tr("Signal"), stack)
    }

    @JvmStatic
    fun addResource(stack: ItemStack) {
        add("Resource", stack)
    }
}

package mods.eln.item

import mods.eln.i18n.I18N.tr
import mods.eln.misc.Utils
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class ThermalIsolatorElement(name: String, val conductionFactor: Double, val Tmax: Double) : GenericItemUsingDamageDescriptorUpgrade(name) {

    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(tr("Upgrade for the Stone Heat Furnace"))
        list.add(tr("Organic Asbestos™"))
        list.add(Utils.plotCelsius(tr("  Heat tolerance:"), Tmax))
        list.add(Utils.plotPercent(tr("  Insulation:"), conductionFactor))
    }
}

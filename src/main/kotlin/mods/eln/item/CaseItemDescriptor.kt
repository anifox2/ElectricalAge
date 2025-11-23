package mods.eln.item

import mods.eln.i18n.I18N.tr
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

import net.minecraft.network.chat.Component

class CaseItemDescriptor(name: String) : GenericItemUsingDamageDescriptorUpgrade(name) {
    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(Component.literal(tr("Can be used to encase EA items that support it")))
    }
}

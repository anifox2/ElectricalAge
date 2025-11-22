package mods.eln.generic

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import mods.eln.i18n.I18N

class GenericItemUsingDamageDescriptorWithComment(name: String, val description: Array<String>) : GenericItemUsingDamageDescriptor(name) {

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, level, tooltip, flag)
        for (str in description) {
            val translated = I18N.tr(str)
            val lines = translated.split("\n")
            for (line in lines) {
                tooltip.add(TextComponent(line))
            }
        }
    }
}

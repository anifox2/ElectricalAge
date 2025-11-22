package mods.eln.item.electricalitem

import mods.eln.i18n.I18N.tr
import mods.eln.item.electricalitem.TreeCapitation.removeBlockWithDrops
import mods.eln.misc.Utils
import mods.eln.wiki.Data
import net.minecraft.world.level.block.Block
import net.minecraft.block.material.Material
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class ElectricalPickaxe(name: String, strengthOn: Float, strengthOff: Float,
                        energyStorage: Double, energyPerBlock: Double, chargePower: Double) : ElectricalTool(name, strengthOn, strengthOff, energyStorage, energyPerBlock, chargePower) {

    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(tr("Opens holes. Right-click to open smaller holes."))
    }

    override fun setParent(item: Item?, damage: Int) {
        super.setParent(item, damage)
        Data.addPortable(newItemStack())
    }

    override fun getStrVsBlock(stack: ItemStack, block: Block?): Float {
        var value = when {
            block != null && (block.material === Material.iron || block.material === Material.glass || block.material === Material.anvil || block.material === Material.rock) -> getStrength(stack)
            else -> super.getStrVsBlock(stack, block)
        }
        if (blocksEffectiveAgainst.any { it == block }) {
            value = getStrength(stack)
        }
        return value
    }

    override fun onItemRightClick(s: ItemStack, w: World, p: Player): ItemStack {
        if (!w.isRemote) {
            setConservative(p, s, !getConservative(s))
        }
        return s
    }

    private fun getConservative(s: ItemStack) =
        getNbt(s).getBoolean("conservative")

    private fun setConservative(p: Player?, s: ItemStack, state: Boolean) {
        getNbt(s).setBoolean("conservative", state)
        if (p != null) {
            Utils.addChatMessage(p, "Set land conservation to $state")
        }
    }

    override fun onBlockDestroyed(stack: ItemStack, w: World, block: Block, x: Int, y: Int, z: Int, entity: LivingEntity): Boolean {
        val ok = super.onBlockDestroyed(stack, w, block, x, y, z, entity)
        if (entity !is Player) return ok
        if (!ok) return ok
        if (!getConservative(stack)) {
            for (a in (-1..1)) {
                for (b in (-1..0)) {
                    for (c in (-1..1)) {
                        if (a == 0 && b == 0 && c == 0) continue
                        val i = x+a
                        val j = y+b
                        val k = z+c
                        removeBlockWithDrops(entity, this, stack, w, i, j, k)
                    }
                }
            }
        }
        return ok
    }
}

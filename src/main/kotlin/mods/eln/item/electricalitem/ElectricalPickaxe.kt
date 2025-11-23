package mods.eln.item.electricalitem

import mods.eln.i18n.I18N.tr
import mods.eln.item.electricalitem.TreeCapitation.removeBlockWithDrops
import mods.eln.misc.Utils
import mods.eln.wiki.Data
import net.minecraft.world.level.block.Block
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.tags.BlockTags
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionHand

class ElectricalPickaxe(name: String, strengthOn: Float, strengthOff: Float,
                        energyStorage: Double, energyPerBlock: Double, chargePower: Double) : ElectricalTool(name, strengthOn, strengthOff, energyStorage, energyPerBlock, chargePower) {

    override fun appendHoverText(itemStack: ItemStack, level: Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(net.minecraft.network.chat.Component.literal(tr("Opens holes. Right-click to open smaller holes.")))
    }

    /*
    override fun setParent(item: Any?, damage: Int) {
        super.setParent(item, damage)
        // Data.addPortable(newItemStack())
    }
    */

    override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float {
        if (state.`is`(BlockTags.MINEABLE_WITH_PICKAXE) || state.`is`(BlockTags.MINEABLE_WITH_SHOVEL)) {
             return getStrength(stack)
        }
        if (ElectricalTool.blocksEffectiveAgainst.contains(state.block)) {
            return getStrength(stack)
        }
        return super.getDestroySpeed(stack, state)
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        if (!level.isClientSide) {
            setConservative(player, stack, !getConservative(stack))
        }
        return InteractionResultHolder.success(stack)
    }

    private fun getConservative(s: ItemStack) =
        getNbt(s).getBoolean("conservative")

    private fun setConservative(p: Player?, s: ItemStack, state: Boolean) {
        getNbt(s).putBoolean("conservative", state)
        if (p != null) {
            Utils.addChatMessage(p, "Set land conservation to $state")
        }
    }

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, entity: LivingEntity): Boolean {
        val ok = super.mineBlock(stack, world, state, pos, entity)
        if (entity !is Player) return ok
        if (!ok) return ok
        if (!getConservative(stack)) {
            for (a in (-1..1)) {
                for (b in (-1..0)) {
                    for (c in (-1..1)) {
                        if (a == 0 && b == 0 && c == 0) continue
                        val targetPos = pos.offset(a, b, c)
                        TreeCapitation.removeBlockWithDrops(entity, this, stack, world, targetPos)
                    }
                }
            }
        }
        return ok
    }

    fun getNbt(stack: ItemStack): net.minecraft.nbt.CompoundTag {
        return stack.orCreateTag
    }
    
    override fun getTransferRate(stack: ItemStack): Double {
        return chargePower
    }
}

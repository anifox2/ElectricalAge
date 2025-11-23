package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.i18n.I18N.tr
import mods.eln.misc.Utils
import mods.eln.misc.UtilsClient
import mods.eln.wiki.Data
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.network.chat.Component
// import net.minecraftforge.client.IItemRenderer

import org.lwjgl.opengl.GL11

class BrushDescriptor(name: String): GenericItemUsingDamageDescriptor(name) {

    private val ricon = ResourceLocation("eln", "textures/items/" + name.lowercase().replace(" ", "") + ".png")


    override fun getName(stack: ItemStack): Component {
        val creative = Minecraft.getInstance().player?.isCreative() ?: false
        val color = getColor(stack)
        val life = getLife(stack)
        return if (!creative && color == 15 && life == 0) Component.literal("Empty ").append(super.getName(stack)) else super.getName(stack)
    }

    override fun setParent(registry: Any?, id: Int) {
        super.setParent(registry, id)
        Data.addWiring(newItemStack())
    }

    fun getColor(stack: ItemStack) = stack.damageValue and 0xF

    private fun getLife(stack: ItemStack?) = if (stack == null || stack.tag == null)
        32
    else
        stack.tag!!.getInt("life")

    fun setLife(stack: ItemStack, life: Int) {
        stack.getOrCreateTag().putInt("life", life)
    }

    override fun getDefaultNBT(): CompoundTag? {
        val nbt = CompoundTag()
        nbt.putInt("life", 32)
        return nbt
    }

    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)

        if (itemStack != null) {
            val creative = Minecraft.getInstance().player?.isCreative() ?: false
            val text = tr("Can paint %1$ blocks", if (creative) "infinite" else itemStack.tag?.getInt("life") ?: 32)
            list.add(Component.literal(text))
        }
    }

    fun use(stack: ItemStack, entityPlayer: Player): Boolean {

        val creative = entityPlayer.isCreative()
        var life = stack.tag?.getInt("life") ?: 32
        return if (creative || life != 0) {
            if (!creative) {
                --life
                stack.getOrCreateTag().putInt("life", life)
            }
            true
        } else {
            Utils.addChatMessage(entityPlayer, tr("Brush is dry"))
            false
        }
    }

    /*
    override fun handleRenderType(item: ItemStack?, type: IItemRenderer.ItemRenderType?) = type == IItemRenderer.ItemRenderType.INVENTORY

    override fun shouldUseRenderHelper(type: IItemRenderer.ItemRenderType?, item: ItemStack?, helper: IItemRenderer.ItemRendererHelper?) =
        type != IItemRenderer.ItemRenderType.INVENTORY

    override fun renderItem(type: IItemRenderer.ItemRenderType?, item: ItemStack?, vararg data: Any?) {
        if (type == IItemRenderer.ItemRenderType.INVENTORY) {
            val creative = Minecraft.getInstance().player.capabilities.isCreativeMode
            UtilsClient.drawIcon(type, ricon)
            if (!creative) {
                GL11.glColor4f(1f, 1f, 1f, 0.75f - 0.75f * getLife(item) / 32f)
                UtilsClient.drawIcon(type, dryOverlay)
                GL11.glColor3f(1f, 1f, 1f)
            }
        } else {
            super.renderItem(type, item, *data)
        }
    }
    */


    companion object {
        private val dryOverlay = ResourceLocation("eln", "textures/items/brushdryoverlay.png")
    }
}

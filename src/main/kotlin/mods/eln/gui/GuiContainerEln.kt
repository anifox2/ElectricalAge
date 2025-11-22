package mods.eln.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import com.mojang.blaze3d.systems.RenderSystem

abstract class GuiContainerEln<T : AbstractContainerMenu>(menu: T, inventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, inventory, title), IGuiObject {
    var helper: GuiHelperContainer? = null

    override fun init() {
        super.init()
        helper = newHelper()
        initGui()
    }

    open fun initGui() {}

    abstract fun newHelper(): GuiHelperContainer

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        preDraw(guiGraphics, partialTick, mouseX, mouseY)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        postDraw(guiGraphics, partialTick, mouseX, mouseY)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    open fun preDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {}
    open fun postDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {}

    override fun guiObjectEvent(eventId: Int) {}

    fun newGuiVerticalProgressBar(x: Int, y: Int, w: Int, h: Int): GuiVerticalProgressBar {
        return GuiVerticalProgressBar(x, y, w, h)
    }

    fun setColor(r: Float, g: Float, b: Float) {
        RenderSystem.setShaderColor(r, g, b, 1.0f)
    }

    fun drawString(guiGraphics: GuiGraphics, x: Int, y: Int, text: String, color: Int) {
        guiGraphics.drawString(font, text, x, y, color, false)
    }
}

package mods.eln.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import com.mojang.blaze3d.systems.RenderSystem
import mods.eln.gui.GuiButtonEln
import mods.eln.gui.GuiTextFieldEln
import mods.eln.gui.GuiVerticalTrackBar
import mods.eln.gui.GuiVerticalCustomValuesBar

abstract class GuiContainerEln<T : AbstractContainerMenu>(menu: T, inventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, inventory, title), IGuiObject {
    @JvmField
    var helper: GuiHelperContainer? = null

    override fun init() {
        super.init()
        helper = newHelper()
        helper?.let {
            imageWidth = it.xSize
            imageHeight = it.ySize
            leftPos = (width - imageWidth) / 2
            topPos = (height - imageHeight) / 2
        }
        initGui()
    }

    open fun initGui() {}

    abstract fun newHelper(): GuiHelperContainer

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        helper?.drawBackground(guiGraphics, leftPos, topPos)
    }

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
    open fun guiObjectEvent(obj: IGuiObject) {}

    fun newGuiVerticalProgressBar(x: Int, y: Int, w: Int, h: Int): GuiVerticalProgressBar {
        return GuiVerticalProgressBar(x, y, w, h)
    }

    fun setColor(r: Float, g: Float, b: Float) {
        RenderSystem.setShaderColor(r, g, b, 1.0f)
    }

    fun drawString(guiGraphics: GuiGraphics, x: Int, y: Int, text: String, color: Int) {
        guiGraphics.drawString(font, text, x, y, color, false)
    }

    fun newGuiTextField(x: Int, y: Int, width: Int): GuiTextFieldEln {
        val tf = GuiTextFieldEln(font, leftPos + x, topPos + y, width, 20, Component.empty())
        addRenderableWidget(tf)
        return tf
    }

    @JvmOverloads
    fun newGuiButton(x: Int, y: Int, width: Int, text: String, onPress: net.minecraft.client.gui.components.Button.OnPress? = null): GuiButtonEln {
        val actualOnPress = onPress ?: net.minecraft.client.gui.components.Button.OnPress { btn ->
            if (btn is IGuiObject) {
                this.guiObjectEvent(btn)
            }
        }
        val btn = GuiButtonEln(leftPos + x, topPos + y, width, 20, text, actualOnPress)
        addRenderableWidget(btn)
        return btn
    }

    fun newGuiVerticalTrackBar(x: Int, y: Int, width: Int, height: Int): GuiVerticalTrackBar {
        return GuiVerticalTrackBar(leftPos + x, topPos + y, width, height, helper)
    }

    fun newGuiVerticalCustomValuesBar(x: Int, y: Int, width: Int, height: Int, positions: Array<Float>): GuiVerticalCustomValuesBar {
        return GuiVerticalCustomValuesBar(leftPos + x, topPos + y, width, height, helper!!, positions)
    }
}

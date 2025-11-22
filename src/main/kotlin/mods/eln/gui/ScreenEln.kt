package mods.eln.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import com.mojang.blaze3d.systems.RenderSystem

open class ScreenEln(title: Component = Component.empty()) : Screen(title), IGuiObject {
    var helper: GuiHelperContainer? = null
    var leftPos: Int = 0
    var topPos: Int = 0

    override fun init() {
        super.init()
        helper = newHelper()
        initGui()
        if (helper != null) {
            leftPos = (width - helper!!.width) / 2
            topPos = (height - helper!!.ySize) / 2
        }
    }

    open fun initGui() {}

    open fun newHelper(): GuiHelperContainer {
        return GuiHelperContainer(this, 0, 0)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)
        helper?.render(guiGraphics, leftPos, topPos)
        preDraw(guiGraphics, partialTick, mouseX, mouseY)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        postDraw(guiGraphics, partialTick, mouseX, mouseY)
    }

    open fun preDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {}
    open fun postDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {}

    override fun guiObjectEvent(eventId: Int) {}

    fun newGuiVerticalProgressBar(x: Int, y: Int, w: Int, h: Int): GuiVerticalProgressBar {
        return GuiVerticalProgressBar(x, y, w, h)
    }

    fun newGuiTextField(x: Int, y: Int, width: Int): GuiTextFieldEln {
        val tf = GuiTextFieldEln(font, leftPos + x, topPos + y, width, 20, Component.empty())
        addRenderableWidget(tf)
        return tf
    }

    fun newGuiButton(x: Int, y: Int, width: Int, text: String, onPress: (net.minecraft.client.gui.components.Button) -> Unit = {}): GuiButtonEln {
        val btn = GuiButtonEln(leftPos + x, topPos + y, width, 20, text, onPress)
        addRenderableWidget(btn)
        return btn
    }

    fun newGuiVerticalTrackBar(x: Int, y: Int, width: Int, height: Int): GuiVerticalTrackBar {
        return GuiVerticalTrackBar(leftPos + x, topPos + y, width, height, helper)
    }

    fun newGuiVerticalCustomValuesBar(x: Int, y: Int, width: Int, height: Int, positions: Array<Float>): GuiVerticalCustomValuesBar {
        return GuiVerticalCustomValuesBar(leftPos + x, topPos + y, width, height, helper!!, positions)
    }

    fun setColor(r: Float, g: Float, b: Float) {
        RenderSystem.setShaderColor(r, g, b, 1.0f)
    }

    fun drawString(guiGraphics: GuiGraphics, x: Int, y: Int, text: String, color: Int) {
        guiGraphics.drawString(font, text, x, y, color, false)
    }
}
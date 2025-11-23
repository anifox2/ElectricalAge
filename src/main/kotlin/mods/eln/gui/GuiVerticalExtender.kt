package mods.eln.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class GuiVerticalExtender(
    x: Int, y: Int, width: Int, height: Int,
    var helper: GuiHelperContainer?
) : AbstractWidget(x, y, width, height, Component.empty()) {

    fun add(component: Any) {
        // Stub
    }
    
    fun getSliderPosition(): Float {
        return 0f
    }
    
    fun setSliderPosition(pos: Float) {
        // Stub
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Stub
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
}

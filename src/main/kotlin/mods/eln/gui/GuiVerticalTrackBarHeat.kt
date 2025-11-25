package mods.eln.gui

import net.minecraft.client.gui.GuiGraphics

class GuiVerticalTrackBarHeat(x: Int, y: Int, width: Int, height: Int) : GuiVerticalTrackBar(x, y, width, height) {
    var temperatureHit: Double = 0.0

    override fun draw(guiGraphics: GuiGraphics, x: Int, y: Int) {
        super.draw(guiGraphics, x, y)

        if (!visible) return

        val left = x + this.x
        val top = y + this.y
        val right = left + width
        val bottom = top + height

        // Draw temperature indicator (fill from hit point downwards)
        val range = rangeMax - rangeMin
        val pct = if (range == 0f) 0f else ((temperatureHit.toFloat() - rangeMin) / range).coerceIn(0f, 1f)

        val indicatorY = bottom - (pct * height).toInt()
        guiGraphics.fill(left + 2, indicatorY, right - 2, bottom, 0xAAFF2020.toInt())
    }
}

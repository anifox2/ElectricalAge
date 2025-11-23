package mods.eln.gui

import net.minecraft.client.gui.GuiGraphics

class GuiVerticalProgressBar(val x: Int, val y: Int, val width: Int, val height: Int) {
    var value: Float = 0.0f
    var r: Float = 1.0f
    var g: Float = 1.0f
    var b: Float = 1.0f
    var comment: String = ""

    fun setColor(r: Float, g: Float, b: Float) {
        this.r = r
        this.g = g
        this.b = b
    }

    fun setComment(index: Int, comment: String) {
        this.comment = comment
    }

    fun draw(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int) {
        // Draw background
        guiGraphics.fill(offsetX + x, offsetY + y, offsetX + x + width, offsetY + y + height, 0xFF000000.toInt())
        // Draw bar
        val barHeight = (height * value).toInt()
        val color = (0xFF shl 24) or ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or ((b * 255).toInt())
        guiGraphics.fill(offsetX + x, offsetY + y + height - barHeight, offsetX + x + width, offsetY + y + height, color)
    }
}

package mods.eln.gui

import net.minecraft.client.gui.GuiGraphics

open class GuiVerticalTrackBar(val x: Int, val y: Int, val width: Int, val height: Int, val helper: GuiHelper? = null) {
    open var value: Float = 0.0f
    
    open fun draw(guiGraphics: GuiGraphics, x: Int, y: Int) {
        // Stub
    }
    
    fun setRange(min: Float, max: Float) {}
    fun setStep(step: Float) {}
    fun setEnable(enable: Boolean) {}
    fun setStepIdMax(max: Int) {}
    fun setComment(index: Int, comment: String) {}
    var pending: Boolean = false
}

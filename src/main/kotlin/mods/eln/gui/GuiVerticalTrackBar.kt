package mods.eln.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

open class GuiVerticalTrackBar(val x: Int, val y: Int, val width: Int, val height: Int, val helper: GuiHelper? = null) {
    var visible: Boolean = true
    var enabled: Boolean = true
        set(value) {
            field = value
            if (!value) dragging = false
        }
    var rangeMin: Float = 0.0f
    var rangeMax: Float = 1.0f
    private var stepId: Int = 0
    private var stepIdMax: Int = 10
    private val comments = mutableListOf<String>()
    private var dragging = false
    var pending: Boolean = false

    open var value: Float
        get() = rangeMin + (rangeMax - rangeMin) * stepId / stepIdMax
        set(v) {
            if (!dragging) {
                setStepForValue(v)
            }
        }

    fun setRange(min: Float, max: Float) {
        rangeMin = min
        rangeMax = max
        clampStep()
    }

    fun setEnable(enable: Boolean) {
        this.enabled = enable
    }

    fun setStepIdMax(max: Int) {
        stepIdMax = max
        clampStep()
    }

    fun setComment(index: Int, comment: String) {
        if (index < comments.size) {
            comments[index] = comment
        } else {
            while (comments.size < index) comments.add("")
            comments.add(comment)
        }
    }

    fun setStep(step: Float) {
        if (step > 0f) {
            val steps = ((rangeMax - rangeMin) / step).roundToInt().coerceAtLeast(1)
            setStepIdMax(steps)
        }
    }

    open fun draw(guiGraphics: GuiGraphics, x: Int, y: Int) {
        if (!visible) return

        val left = x + this.x
        val top = y + this.y
        val right = left + width
        val bottom = top + height

        // Track (beveled look similar to 1.7.10)
        val outer = 0xFF404040.toInt()
        val mid = 0xFF606060.toInt()
        val inner = 0xFF808080.toInt()
        guiGraphics.fill(left, top - 2, right, bottom + 2, outer)
        guiGraphics.fill(left + 1, top - 1, right - 1, bottom + 1, mid)
        guiGraphics.fill(left + 2, top, right - 2, bottom, inner)

        // Knob
        val knobHeight = 4
        val pct = if (rangeMax == rangeMin) 0f else (value - rangeMin) / (rangeMax - rangeMin)
        val knobY = top + height - knobHeight - (pct * (height - knobHeight)).toInt()
        val knobBorder = 0xFF202020.toInt()
        val knobFill = if (enabled) 0xFFC0C0C0.toInt() else 0xFF606060.toInt()
        guiGraphics.fill(left - 1, knobY - 1, right + 1, knobY + knobHeight + 1, knobBorder)
        guiGraphics.fill(left, knobY, right, knobY + knobHeight, knobFill)
    }

    fun renderTooltip(guiGraphics: GuiGraphics, font: Font, mouseX: Int, mouseY: Int, left: Int, top: Int) {
        if (!visible) return
        if (!isMouseOver(mouseX, mouseY, left, top)) return
        val tooltip = comments.filter { it.isNotBlank() }.map { Component.literal(it).visualOrderText }
        if (tooltip.isNotEmpty()) {
            guiGraphics.renderTooltip(font, tooltip, mouseX, mouseY)
        }
    }

    fun handleMouseClicked(mouseX: Int, mouseY: Int, button: Int, left: Int, top: Int): Boolean {
        if (button != 0 || !visible || !enabled) return false
        if (!isMouseOver(mouseX, mouseY, left, top)) return false
        dragging = true
        updateValueFromMouse(mouseY, top)
        return true
    }

    fun handleMouseDragged(mouseX: Int, mouseY: Int, button: Int, left: Int, top: Int): Boolean {
        if (!dragging || button != 0) return false
        updateValueFromMouse(mouseY, top)
        return true
    }

    fun handleMouseReleased(mouseX: Int, mouseY: Int, button: Int, left: Int, top: Int): Boolean {
        if (button != 0) return false
        if (!dragging) return false
        updateValueFromMouse(mouseY, top)
        dragging = false
        return true
    }

    private fun updateValueFromMouse(mouseY: Int, top: Int) {
        val barTop = top + y
        val rel = (mouseY - barTop).coerceIn(0, height)
        val newStepId = (stepIdMax - (rel.toFloat() / height) * stepIdMax).roundToInt().coerceIn(0, stepIdMax)
        if (newStepId != stepId) {
            stepId = newStepId
            pending = true
        }
    }

    private fun setStepForValue(v: Float) {
        val range = (rangeMax - rangeMin).takeIf { it != 0f } ?: return
        val pct = ((v - rangeMin) / range).coerceIn(0f, 1f)
        stepId = (pct * stepIdMax).roundToInt().coerceIn(0, stepIdMax)
    }

    private fun clampStep() {
        stepId = stepId.coerceIn(0, stepIdMax)
    }

    private fun isMouseOver(mouseX: Int, mouseY: Int, left: Int, top: Int): Boolean {
        val lx = left + x
        val ly = top + y
        return mouseX in lx until (lx + width) && mouseY in ly until (ly + height)
    }
}

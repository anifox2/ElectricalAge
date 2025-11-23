package mods.eln.gui

import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import net.minecraft.client.gui.components.Tooltip

class GuiButtonEln(x: Int, y: Int, width: Int, height: Int, message: String, onPress: Button.OnPress) : Button(x, y, width, height, Component.literal(message), onPress, DEFAULT_NARRATION), IGuiObject {
    
    var enabled: Boolean
        get() = this.active
        set(value) { this.active = value }

    var displayString: String
        get() = this.message.string
        set(value) { this.message = Component.literal(value) }

    fun setComment(index: Int, comment: String) {
        this.tooltip = Tooltip.create(Component.literal(comment))
    }

    fun setComment(comment: Array<String>) {
        if (comment.isNotEmpty()) {
            // Join with newlines or just take first?
            // Tooltip.create takes Component.
            // We can create a multiline tooltip.
            val text = comment.joinToString("\n")
            this.tooltip = Tooltip.create(Component.literal(text))
        }
    }

    fun clearComment() {
        this.tooltip = null
    }

    override fun guiObjectEvent(eventId: Int) {}
}

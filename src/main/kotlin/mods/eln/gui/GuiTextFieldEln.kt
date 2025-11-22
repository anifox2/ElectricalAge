package mods.eln.gui

import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.client.gui.components.Tooltip

class GuiTextFieldEln(font: Font, x: Int, y: Int, width: Int, height: Int, title: Component) : EditBox(font, x, y, width, height, title), IGuiObject {

    fun interface GuiTextFieldElnObserver {
        fun textFieldNewValue(textField: GuiTextFieldEln, value: String)
    }

    var observer: GuiTextFieldElnObserver? = null

    var enabled: Boolean
        get() = this.active
        set(value) { this.active = value }

    init {
        this.setResponder { value ->
            observer?.textFieldNewValue(this, value)
        }
    }

    override fun guiObjectEvent(eventId: Int) {
    }

    fun setText(text: String) {
        this.value = text
    }

    var text: String
        get() = this.value
        set(value) { this.value = value }

    fun setComment(comment: Array<String>) {
        if (comment.isNotEmpty()) {
            this.tooltip = Tooltip.create(Component.literal(comment.joinToString("\n")))
        }
    }
    
    fun setObserver(observer: GuiTextFieldElnObserver) {
        this.observer = observer
    }
}

package mods.eln.gui

class Synchronizable(initialValue: Float) {
    var pending = false
    var value: Float = initialValue
        set(newValue) {
            pending = newValue != field
            field = newValue
        }
        get() {
            pending = false
            return field
        }
}

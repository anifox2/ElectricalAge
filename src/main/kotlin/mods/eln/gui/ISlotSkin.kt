package mods.eln.gui

interface ISlotSkin {
    enum class SlotSkin {
        none,
        medium,
        large,
        small,
        output,
        furnace
    }
    fun getSlotSkin(): SlotSkin
}

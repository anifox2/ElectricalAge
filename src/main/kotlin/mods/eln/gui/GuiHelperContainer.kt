package mods.eln.gui

import net.minecraft.client.gui.screens.Screen

open class GuiHelperContainer(screen: Screen?, width: Int, height: Int, val xInv: Int = 0, val yInv: Int = 0, backgroundName: String? = null) : GuiHelper(screen, width, height, backgroundName) {
}

package mods.eln.gui

import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation
import java.util.ArrayList

open class GuiHelper(val screen: Screen?, val width: Int, val height: Int, backgroundName: String? = null) {
    val objectList = ArrayList<IGuiObject>()
    val drawables = ArrayList<(net.minecraft.client.gui.GuiGraphics, Int, Int) -> Unit>()
    var background: ResourceLocation? = null
    var xSize = width
    var ySize = height

    init {
        if (backgroundName != null) {
            background = ResourceLocation("eln", "textures/gui/$backgroundName")
        }
    }

    fun add(o: IGuiObject) {
        objectList.add(o)
    }

    fun drawBackground(guiGraphics: net.minecraft.client.gui.GuiGraphics, x: Int, y: Int) {
        if (background != null) {
            guiGraphics.blit(background!!, x, y, 0, 0, xSize, ySize)
        }
    }

    fun drawString(guiGraphics: net.minecraft.client.gui.GuiGraphics, x: Int, y: Int, text: String, color: Int) {
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, text, x, y, color, false)
    }

    fun drawRect(x1: Int, y1: Int, x2: Int, height: Int, color: Int) {
        drawables.add { guiGraphics, offX, offY ->
            // Assume color is grayscale 0-255
            val c = 0xFF000000.toInt() or (color shl 16) or (color shl 8) or color
            guiGraphics.fill(offX + x1, offY + y1, offX + x2, offY + y1 + height, c)
        }
    }

    fun render(guiGraphics: net.minecraft.client.gui.GuiGraphics, x: Int, y: Int) {
        drawBackground(guiGraphics, x, y)
        for (d in drawables) {
            d(guiGraphics, x, y)
        }
    }
}

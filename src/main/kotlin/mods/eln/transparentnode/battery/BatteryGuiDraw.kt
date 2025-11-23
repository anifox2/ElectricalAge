package mods.eln.transparentnode.battery

import mods.eln.gui.GuiHelperContainer
import mods.eln.gui.ScreenEln
import mods.eln.gui.GuiVerticalProgressBar
import mods.eln.gui.HelperStdContainer
import mods.eln.gui.IGuiObject
import mods.eln.i18n.I18N.tr
import mods.eln.misc.Utils

import net.minecraft.client.gui.GuiGraphics

class BatteryGuiDraw(var render: BatteryRender) : ScreenEln() {
    var energyBar: GuiVerticalProgressBar? = null

    override fun initGui() {
        super.initGui()
        super.helper!!.ySize = 50
        energyBar = newGuiVerticalProgressBar(167 - 16, 8, 16, 35)
        energyBar!!.setColor(0.2f, 0.5f, 0.8f)
    }

    override fun guiObjectEvent(eventId: Int) {
        super.guiObjectEvent(eventId)
    }

    override fun preDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        super.preDraw(guiGraphics, f, x, y)
        energyBar!!.value = (render.energy / (render.descriptor.electricalStdEnergy * render.life)).toFloat()
        energyBar!!.setComment(0, Utils.plotPercent(tr("Energy: "), energyBar!!.value.toDouble()).replace(" ", ""))
        energyBar!!.draw(guiGraphics, leftPos, topPos)
    }

    override fun postDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        super.postDraw(guiGraphics, f, x, y)
        val str1: String
        var str2: String? = ""
        val p = render.power.toDouble()
        val energyMiss = render.descriptor.electricalStdEnergy * render.life - render.energy
        when {
            Math.abs(p) < 5 -> {
                str1 = tr("No charge")
            }
            p > 0 -> {
                str1 = tr("Discharge")
                str2 = Utils.plotTime("", render.energy / p)
            }
            energyMiss > 0 -> {
                str1 = tr("Charge")
                str2 = Utils.plotTime("", -energyMiss / p)
            }
            else -> {
                str1 = tr("Charged")
            }
        }
        val xDelta = 70
        if (render.descriptor.lifeEnable) {
            drawString(guiGraphics, 8, 8, tr("Life:"), 0xFF000000.toInt())
            drawString(guiGraphics, xDelta, 8, Utils.plotPercent("", render.life.toDouble()), 0xFF000000.toInt())
        }
        drawString(guiGraphics, 8, 17, tr("Energy:"), 0xFF000000.toInt())
        drawString(guiGraphics, xDelta, 17,
            Utils.plotValue(render.energy.toDouble(), "J/") + Utils.plotValue(render.descriptor.electricalStdEnergy * render.life, "J"), 0xFF000000.toInt())
        if (render.power >= 0) drawString(guiGraphics, 8, 26, tr("Power out:"), 0xFF000000.toInt()) else drawString(guiGraphics, 8, 26, tr("Power in:"), 0xFF000000.toInt())
        drawString(guiGraphics, xDelta, 26, Utils.plotValue(Math.abs(render.power).toDouble(), "W/") + Utils.plotValue(render.descriptor.electricalStdP, "W"), 0xFF000000.toInt())
        drawString(guiGraphics, 8, 35, str1, 0xFF000000.toInt())
        if (str2 != null) drawString(guiGraphics, xDelta, 35, str2, 0xFF000000.toInt())
    }

    override fun newHelper(): GuiHelperContainer {
        return HelperStdContainer(this)
    }
}

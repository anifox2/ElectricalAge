package mods.eln.sixnode.powersocket

import mods.eln.gui.GuiHelperContainer
import mods.eln.gui.ScreenEln
import mods.eln.gui.GuiTextFieldEln
import mods.eln.gui.IGuiObject
import mods.eln.i18n.I18N.tr
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container

class PowerSocketGui(private val render: PowerSocketRender, player: Player?, inventory: Container?) :
    ScreenEln() {

        var device: GuiTextFieldEln? = null
    override fun initGui() {
        super.initGui()

        // ...existing code...
        device = newGuiTextField(8, 8, 138)
        device?.text = render.channel ?: ""
        device?.setComment(arrayOf(tr("Specify the power channel")))
        // ...existing code...
        device?.observer = GuiTextFieldEln.GuiTextFieldElnObserver { _, text ->
            render.clientSetString(PowerSocketElement.setChannelId, text)
        }
    }

    override fun newHelper(): GuiHelperContainer {
        return GuiHelperContainer(this, 154, 30, 0, 0)
    }
}

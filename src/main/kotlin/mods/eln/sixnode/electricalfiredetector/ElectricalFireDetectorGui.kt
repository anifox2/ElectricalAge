package mods.eln.sixnode.electricalfiredetector

import mods.eln.gui.GuiContainerEln
import mods.eln.gui.GuiHelperContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container

import net.minecraft.network.chat.Component

import net.minecraft.client.gui.GuiGraphics

class ElectricalFireDetectorGui(player: Player, inventory: Container, var render: ElectricalFireDetectorRender)
    : GuiContainerEln<ElectricalFireDetectorContainer>(
        ElectricalFireDetectorContainer(player, inventory),
        player.inventory,
        Component.literal("Fire Detector")
    ) {
    override fun newHelper(): GuiHelperContainer = GuiHelperContainer(this, 176, 166 - 52, 8, 84 - 52)

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        helper!!.drawBackground(guiGraphics, 176, 166)
    }
}

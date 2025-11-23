package mods.eln.sixnode.lampsocket

import mods.eln.gui.GuiContainerEln
import mods.eln.gui.GuiHelperContainer
import mods.eln.gui.GuiTextFieldEln
import mods.eln.gui.GuiVerticalTrackBar
import mods.eln.gui.HelperStdContainer
import mods.eln.gui.IGuiObject
import mods.eln.i18n.I18N.tr
import mods.eln.node.six.SixNodeElementInventory
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container

class LampSocketGuiDraw(player: Player, inventory: Container, var lampRender: LampSocketRender) : GuiContainerEln<LampSocketContainer>(LampSocketContainer(player, inventory, lampRender.lampSocketDescriptor), player.inventory, Component.literal("Lamp Socket")) {

    var inventory: SixNodeElementInventory = inventory as SixNodeElementInventory
    var buttonGrounded: Button? = null
    var buttonSupplyType: Button? = null
    var channel: GuiTextFieldEln? = null
    var alphaZ: GuiVerticalTrackBar? = null

    override fun initGui() {
        super.initGui()
        var x = 0
        if (lampRender.descriptor.alphaZMax == lampRender.descriptor.alphaZMin) {
            x = -0
            buttonSupplyType = newGuiButton(x + 176 / 2 - 140 / 2, 8, 140, "") {
                clientSendPacket(LampSocketElement.tooglePowerSupplyType)
            }
            channel = newGuiTextField(x + 176 / 2 - 140 / 2 + 1, 34, 140)
        } else {
            buttonSupplyType = newGuiButton(x + 176 / 2 - 140 / 2 - 12, 8, 136, "") {
                clientSendPacket(LampSocketElement.tooglePowerSupplyType)
            }
            channel = newGuiTextField(x + 176 / 2 - 140 / 2 - 11, 34, 135)
        }

        buttonGrounded = newGuiButton(x + 176 / 2 - 30, -2000, 60, "") {
            clientSendPacket(LampSocketElement.setGroundedId)
        }

        channel!!.setComment(arrayOf(tr("Specify the supply channel")))

        channel!!.value = lampRender.channel ?: ""
        channel!!.observer = GuiTextFieldEln.GuiTextFieldElnObserver { _, _ ->
            clientSendPacket(LampSocketElement.setChannel)
        }

        alphaZ = newGuiVerticalTrackBar(176 - 8 - 20, 8, 20, 69)
        alphaZ!!.setRange(lampRender.descriptor.alphaZMin, lampRender.descriptor.alphaZMax)
        alphaZ!!.setStepIdMax(200)
        alphaZ!!.value = lampRender.alphaZ.toFloat()

        if (lampRender.descriptor.alphaZMax == lampRender.descriptor.alphaZMin) {
            alphaZ!!.visible = false
        }
    }

    fun clientSendPacket(id: Int) {
        val bos = java.io.ByteArrayOutputStream()
        val stream = java.io.DataOutputStream(bos)
        try {
            stream.writeByte(mods.eln.Eln.packetPublishForNode.toInt())
            val pos = lampRender.blockEntity.blockPos
            stream.writeInt(pos.x)
            stream.writeInt(pos.y)
            stream.writeInt(pos.z)
            stream.writeByte(0) // Dimension TODO
            stream.writeUTF(lampRender.blockEntity.nodeUuid)
            stream.writeByte(lampRender.side.int)
            
            when (id) {
                LampSocketElement.setAlphaZId -> stream.writeFloat(alphaZ!!.value)
                LampSocketElement.setChannel -> stream.writeUTF(channel!!.value)
            }
            
            mods.eln.ElnNetwork.sendToServer(mods.eln.ElnPacket(bos.toByteArray()))
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
    }

    override fun guiObjectEvent(eventId: Int) {
        super.guiObjectEvent(eventId)
        if (alphaZ != null && alphaZ!!.pending) {
            alphaZ!!.pending = false
            clientSendPacket(LampSocketElement.setAlphaZId)
        }
    }

    override fun newHelper(): GuiHelperContainer {
        return HelperStdContainer(this)
    }

    override fun preDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        super.preDraw(guiGraphics, f, x, y)
        if (lampRender.grounded)
            buttonGrounded!!.message = Component.literal(tr("Parallel"))
        else
            buttonGrounded!!.message = Component.literal(tr("Serial"))

        if (lampRender.poweredByLampSupply) {
            buttonSupplyType!!.message = Component.literal(tr("Powered by Lamp Supply"))
            channel!!.visible = true
        }
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        helper?.drawBackground(guiGraphics, leftPos, topPos)
    }
}

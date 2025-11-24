package mods.eln.sixnode.hub

import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.entity.player.Player

class HubRender(tileEntity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElementRender(tileEntity, side, descriptor) {

    var descriptor: HubDescriptor = descriptor as HubDescriptor
    var connectionGrid = BooleanArray(6)

    override fun draw() {
        val poseStack = currentPoseStack!!
        val buffer = currentBuffer!!
        val light = currentLight
        val overlay = currentOverlay
        val consumer = buffer.getBuffer(RenderType.solid())

        // Update connectionGrid
        for (idx in 0 until 4) {
            val lrdu = LRDU.fromInt(idx)
            val render = getCableRender(lrdu)
            if (render != null && connectedSide.mask and (1 shl idx) != 0) {
                connectionGrid[idx] = true
            } else {
                connectionGrid[idx] = false
            }
        }
        connectionGrid[LRDU.Down.toInt()] = true
        connectionGrid[LRDU.Up.toInt()] = false

        poseStack.pushPose()
        front!!.rotatePoseOnX(poseStack)
        
        descriptor.draw(poseStack, consumer, light, overlay, connectionGrid)
        
        poseStack.popPose()
    }

    override fun drawCables() {
        // Deprecated
    }

    override fun newGuiDraw(side: Direction, player: Player): Screen? {
        return null
    }
}

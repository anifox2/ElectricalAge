package mods.eln.transparentnode.heatfurnace

import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import mods.eln.gui.Synchronizable
import java.io.DataInputStream
import net.minecraft.world.entity.player.Player
import mods.eln.misc.Direction

class HeatFurnaceRender(
    entity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {
    
    override val inventory = mods.eln.node.transparent.TransparentNodeElementInventory(1, 64, this)
    
    var burnTime = 0
    var currentItemBurnTime = 0
    var damper = Synchronizable(1.0f)
    var temperature = 0f

    override fun draw() {
        // Legacy draw, not used much
    }

    var doorAngle = 0f

    override fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        val descriptor = transparentNodedescriptor as HeatFurnaceDescriptor
        front?.rotateXnRef(poseStack)
        
        val level = tileEntity.level
        val pos = tileEntity.blockPos
        var targetAngle = 0f
        
        if (level != null) {
            val player = level.getNearestPlayer(pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5, 4.0, false)
            if (player != null) {
                targetAngle = 1.0f
            }
        }
        
        val speed = 0.1f
        if (doorAngle < targetAngle) {
            doorAngle += speed
            if (doorAngle > targetAngle) doorAngle = targetAngle
        } else if (doorAngle > targetAngle) {
            doorAngle -= speed
            if (doorAngle < targetAngle) doorAngle = targetAngle
        }
        
        descriptor.draw(poseStack, bufferSource, packedLight, packedOverlay, doorAngle)
    }

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        burnTime = stream.readInt()
        currentItemBurnTime = stream.readInt()
        damper.value = stream.readFloat()
        temperature = stream.readFloat()
    }
    
    override fun newGuiDraw(side: Direction, player: Player) = null
}

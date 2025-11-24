package mods.eln.transparentnode.solarpanel

import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource

class SolarPanelRender(
    tileEntity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(tileEntity, descriptor) {

    override fun draw() {
        // Legacy draw method, kept for compatibility if needed, but should not be called by new renderer
    }

    override fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        val descriptor = transparentNodedescriptor as SolarPanelDescriptor
        
        front?.rotateXnRef(poseStack)

        val world = tileEntity.level ?: return
        val time = world.dayTime % 24000
        
        // 0 = Sunrise, 6000 = Noon, 12000 = Sunset
        // Map 0..12000 to -90..90 degrees
        var angle = (time - 6000) / 6000f * 90f
        
        // Clamp for night time
        if (time > 12000) {
            angle = -90f // Reset to sunrise position
        } else {
            if (angle < -90f) angle = -90f
            if (angle > 90f) angle = 90f
        }

        descriptor.draw(poseStack, bufferSource, packedLight, packedOverlay, angle)
    }
}

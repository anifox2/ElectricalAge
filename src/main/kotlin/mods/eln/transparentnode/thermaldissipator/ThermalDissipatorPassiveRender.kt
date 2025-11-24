package mods.eln.transparentnode.thermaldissipator

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity

class ThermalDissipatorPassiveRender(
    entity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {

    private val descriptor: ThermalDissipatorPassiveDescriptor = descriptor as ThermalDissipatorPassiveDescriptor

    override fun draw() {
        
        front!!.glRotateXnRef()
        descriptor.draw()
    }

    override fun render(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        front!!.rotateXnRef(poseStack)
        descriptor.draw(poseStack, bufferSource, packedLight, packedOverlay)
    }
}

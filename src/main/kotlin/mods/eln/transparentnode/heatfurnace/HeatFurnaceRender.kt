package mods.eln.transparentnode.heatfurnace

import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource

class HeatFurnaceRender(
    entity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {
    override fun draw() {
        
    }

    override fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        val descriptor = transparentNodedescriptor as HeatFurnaceDescriptor
        front?.rotateXnRef(poseStack)
        descriptor.draw(poseStack, bufferSource, packedLight, packedOverlay)
    }
}

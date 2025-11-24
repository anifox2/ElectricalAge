package mods.eln.transparentnode.turbine

import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import org.lwjgl.opengl.GL11
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType

class TurbineRender(
    entity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {
    override fun draw() {
        val descriptor = transparentNodedescriptor as TurbineDescriptor
        front?.glRotateXnRef()
        descriptor.obj?.draw()
    }

    override fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        val descriptor = transparentNodedescriptor as TurbineDescriptor
        front?.rotateXnRef(poseStack)
        descriptor.obj?.draw(poseStack, bufferSource.getBuffer(RenderType.solid()), packedLight, packedOverlay)
    }
}

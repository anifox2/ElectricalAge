package mods.eln.node.six

import com.mojang.blaze3d.vertex.PoseStack
import mods.eln.misc.Direction.Companion.fromInt
import mods.eln.misc.UtilsClient.glDefaultColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import org.lwjgl.opengl.GL11

class SixNodeRender(context: BlockEntityRendererProvider.Context) : BlockEntityRenderer<SixNodeEntity> {
    override fun render(entity: SixNodeEntity, partialTick: Float, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        Minecraft.getInstance().profiler.push("SixNode")
        
        poseStack.pushPose()
        poseStack.translate(0.5, 0.5, 0.5)
        
        for ((idx, render) in entity.elementRenderList.withIndex()) {
            if (render != null) {
                poseStack.pushPose()
                fromInt(idx).rotatePose(poseStack)
                poseStack.translate(-0.5, 0.0, 0.0)
                
                render.prepareRender(poseStack, bufferSource, packedLight, packedOverlay)
                render.draw()
                
                poseStack.popPose()
            }
        }
        poseStack.popPose()
        Minecraft.getInstance().profiler.pop()
    }
}

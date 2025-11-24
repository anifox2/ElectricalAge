package mods.eln.node.transparent

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import org.lwjgl.opengl.GL11

open class TransparentNodeRender(context: BlockEntityRendererProvider.Context) : BlockEntityRenderer<TransparentNodeBlockEntity> {
    override fun render(entity: TransparentNodeBlockEntity, partialTick: Float, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        if (entity.elementRender == null) return
        
        // Set context for UtilsClient
        mods.eln.misc.UtilsClient.currentPoseStack = poseStack
        mods.eln.misc.UtilsClient.currentBufferSource = bufferSource
        mods.eln.misc.UtilsClient.currentPackedLight = packedLight
        mods.eln.misc.UtilsClient.currentPackedOverlay = packedOverlay
        
        try {
            poseStack.pushPose()
            poseStack.translate(0.5, 0.5, 0.5)
            
            // Note: Mixing PoseStack and GL11 is problematic. 
            // Ideally elementRender.draw() should take PoseStack.
            // For now, we assume elementRender uses GL11 and we might need to sync or just hope.
            // Actually, we should probably use GL11.glPushMatrix() if the inner code uses GL11.
            
            // GL11.glPushMatrix()
            // We need to apply the PoseStack transformation to GL11 if we want them to match, 
            // but since we can't easily extract it, we might just use GL11 for translation too if the inner code relies on it.
            // But render() is called with a relative PoseStack.
            // If we ignore PoseStack and use GL11, we might be drawing at 0,0,0 absolute?
            // No, usually the system sets up GL state.
            
            // GL11.glTranslatef(0.5f, 0.5f, 0.5f)
            entity.elementRender!!.render(poseStack, bufferSource, packedLight, packedOverlay)
            // GL11.glPopMatrix()
            
            poseStack.popPose()
        } finally {
            mods.eln.misc.UtilsClient.currentPoseStack = null
            mods.eln.misc.UtilsClient.currentBufferSource = null
        }
    }
}

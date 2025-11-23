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
        
        val pos = entity.blockPos
        val cameraPos = Minecraft.getInstance().gameRenderer.mainCamera.position
        val x = pos.x - cameraPos.x
        val y = pos.y - cameraPos.y
        val z = pos.z - cameraPos.z

        GL11.glPushMatrix()
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
        
        for ((idx, render) in entity.elementRenderList.withIndex()) {
            if (render != null) {
                glDefaultColor()
                GL11.glPushMatrix()
                fromInt(idx).glRotateXnRef()
                GL11.glTranslatef(-0.5f, 0f, 0f)
                render.draw()
                GL11.glPopMatrix()
            }
        }
        GL11.glPopMatrix()
        Minecraft.getInstance().profiler.pop()
    }
}

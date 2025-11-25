package mods.eln.client

import com.mojang.blaze3d.vertex.PoseStack
import mods.eln.node.six.SixNodeItem
import mods.eln.node.transparent.TransparentNodeItem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

import net.minecraft.resources.ResourceLocation

class ElnItemRenderer : BlockEntityWithoutLevelRenderer(Minecraft.getInstance().blockEntityRenderDispatcher, Minecraft.getInstance().entityModels) {
    companion object {
        val instance = ElnItemRenderer()
    }

    override fun renderByItem(stack: ItemStack, displayContext: ItemDisplayContext, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        val item = stack.item
        if (item is SixNodeItem) {
            val descriptor = item.getDescriptor(stack)
            if (descriptor != null) {
                if (descriptor.iconName != null) {
                    val texture = ResourceLocation("eln", "textures/block/" + descriptor.iconName + ".png")
                    val consumer = buffer.getBuffer(RenderType.text(texture))
                    
                    poseStack.pushPose()
                    // Center the icon
                    poseStack.translate(0.5, 0.5, 0.5)
                    // Scale it to fit
                    poseStack.scale(1.0f, 1.0f, 1.0f)
                    
                    val matrix4f = poseStack.last().pose()
                    // Draw quad centered at 0,0
                    // Invert V coordinates because GUI textures are often flipped relative to world rendering?
                    // Or maybe just standard UVs.
                    consumer.vertex(matrix4f, -0.5f, 0.5f, 0f).color(255, 255, 255, 255).uv(0f, 0f).uv2(packedLight).endVertex()
                    consumer.vertex(matrix4f, 0.5f, 0.5f, 0f).color(255, 255, 255, 255).uv(1f, 0f).uv2(packedLight).endVertex()
                    consumer.vertex(matrix4f, 0.5f, -0.5f, 0f).color(255, 255, 255, 255).uv(1f, 1f).uv2(packedLight).endVertex()
                    consumer.vertex(matrix4f, -0.5f, -0.5f, 0f).color(255, 255, 255, 255).uv(0f, 1f).uv2(packedLight).endVertex()
                    
                    poseStack.popPose()
                } else {
                    val consumer = buffer.getBuffer(RenderType.solid())
                    poseStack.pushPose()
                    descriptor.draw(poseStack, consumer, packedLight, packedOverlay, false)
                    poseStack.popPose()
                }
            }
        } else if (item is TransparentNodeItem) {
            val descriptor = item.getDescriptor(stack)
            if (descriptor != null) {
                if (descriptor.iconName != null) {
                    val texture = ResourceLocation("eln", "textures/blocks/" + descriptor.iconName + ".png")
                    val consumer = buffer.getBuffer(RenderType.text(texture))
                    
                    poseStack.pushPose()
                    // Center the icon
                    poseStack.translate(0.5, 0.5, 0.5)
                    // Scale it to fit
                    poseStack.scale(1.0f, 1.0f, 1.0f)
                    
                    val matrix4f = poseStack.last().pose()
                    // Draw quad centered at 0,0
                    consumer.vertex(matrix4f, -0.5f, 0.5f, 0f).color(255, 255, 255, 255).uv(0f, 1f).uv2(packedLight).endVertex()
                    consumer.vertex(matrix4f, 0.5f, 0.5f, 0f).color(255, 255, 255, 255).uv(1f, 1f).uv2(packedLight).endVertex()
                    consumer.vertex(matrix4f, 0.5f, -0.5f, 0f).color(255, 255, 255, 255).uv(1f, 0f).uv2(packedLight).endVertex()
                    consumer.vertex(matrix4f, -0.5f, -0.5f, 0f).color(255, 255, 255, 255).uv(0f, 0f).uv2(packedLight).endVertex()
                    
                    poseStack.popPose()
                } else {
                    val consumer = buffer.getBuffer(RenderType.solid())
                    poseStack.pushPose()
                    descriptor.draw(poseStack, consumer, packedLight, packedOverlay)
                    poseStack.popPose()
                }
            }
        }
    }
}

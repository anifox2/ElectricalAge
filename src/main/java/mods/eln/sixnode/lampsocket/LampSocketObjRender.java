package mods.eln.sixnode.lampsocket;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public interface LampSocketObjRender {
    void drawItem(LampSocketDescriptor descriptor, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay);
    void draw(LampSocketRender render, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay);
}

package mods.eln.sixnode.lampsocket;

import mods.eln.misc.LRDU;
import mods.eln.misc.Obj3D;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.misc.Utils;
import mods.eln.misc.UtilsClient;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public class LampSocketStandardObjRender implements LampSocketObjRender {

    private Obj3D obj;
    private Obj3DPart socket, socket_unlightable, socket_lightable, lampOn, lampOff, lightAlphaPlane, lightAlphaPlaneNoDepth;
    ResourceLocation tOn, tOff;
    private boolean onOffModel;

    public LampSocketStandardObjRender(Obj3D obj, boolean onOffModel) {
        this.obj = obj;
        this.onOffModel = onOffModel;
        if (obj != null) {
            socket = obj.getPart("socket");
            lampOn = obj.getPart("lampOn");
            lampOff = obj.getPart("lampOff");
            socket_unlightable = obj.getPart("socket_unlightable");
            socket_lightable = obj.getPart("socket_lightable");
            lightAlphaPlane = obj.getPart("lightAlpha");
            lightAlphaPlaneNoDepth = obj.getPart("lightAlphaNoDepth");
            tOff = obj.getModelResourceLocation(obj.getString("tOff"));
            tOn = obj.getModelResourceLocation(obj.getString("tOn"));
        }
    }

    @Override
    public void drawItem(LampSocketDescriptor descriptor, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (descriptor.hasGhostGroup()) {
            poseStack.pushPose();
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
            poseStack.translate(-1.5f, 0f, 0f);
            draw(poseStack, buffer, light, overlay, LRDU.Up, 0, (byte) 0, true, 15, 0.0);
            poseStack.popPose();
        } else {
            draw(poseStack, buffer, light, overlay, LRDU.Up, 0, (byte) 0, true, 15, 0.0);
        }
    }

    @Override
    public void draw(LampSocketRender render, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        int color = 15;
        if (render.getDescriptor().paintable)
            color = render.paintColor;
        
        double distanceToPlayer = 0.0;
        if (render.blockEntity != null && render.blockEntity.getLevel() != null) {
             distanceToPlayer = UtilsClient.distanceFromClientPlayer(render.blockEntity);
        }

        draw(poseStack, buffer, light, overlay, render.front, render.alphaZ, render.light, render.lampDescriptor != null, color, distanceToPlayer);
    }

    public void draw(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, LRDU front, float alphaZ, byte lightLevel, boolean hasBulb, int color, double distanceToPlayer) {
        poseStack.pushPose();
        front.rotatePoseOnX(poseStack);

        // UtilsClient.disableCulling();

        // Utils.setGlColorFromLamp(color);
        // TODO: Handle color tinting if possible with Obj3DPart
        
        if (!onOffModel) {
            if (socket != null) socket.draw(poseStack, buffer, packedLight, packedOverlay);
        } else {
            // TODO: Handle texture swapping (tOn/tOff)
            /*
            if (light > 8) {
                UtilsClient.bindTexture(tOn);
            } else {
                UtilsClient.bindTexture(tOff);
            }
            */
            
            if (socket_unlightable != null) socket_unlightable.draw(poseStack, buffer, packedLight, packedOverlay);

            if (lightLevel > 8) {
                // UtilsClient.disableLight();
                // float l = (light) / 14f;
                // GL11.glColor3f(l, l, l);
                if (socket_lightable != null) socket_lightable.draw(poseStack, buffer, 0xF000F0, packedOverlay); // Full bright
                // GL11.glColor3f(1f, 1f, 1f);
            } else {
                 // Draw socket_lightable as dark? Or not draw it?
                 // Original code only drew it if light > 8.
            }

            if (hasBulb) {
                if (lightLevel > 8) {
                    if (lampOn != null) lampOn.draw(poseStack, buffer, 0xF000F0, packedOverlay);
                } else {
                    if (lampOff != null) lampOff.draw(poseStack, buffer, packedLight, packedOverlay);
                }
            }
            if (socket != null) socket.draw(poseStack, buffer, packedLight, packedOverlay);

            // if (light > 8) UtilsClient.enableLight();
        }

        // UtilsClient.enableBlend();
        // UtilsClient.disableLight();

        if (lightAlphaPlaneNoDepth != null) {
             // TODO: Handle transparency/no depth
             lightAlphaPlaneNoDepth.draw(poseStack, buffer, packedLight, packedOverlay);
        }
        
        poseStack.popPose();
    }
}

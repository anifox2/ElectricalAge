package mods.eln.sixnode.lampsocket;

import mods.eln.Eln;
import mods.eln.misc.LRDU;
import mods.eln.misc.Obj3D;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.misc.UtilsClient;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public class LampSocketSuspendedObjRender implements LampSocketObjRender {

    private Obj3D obj;
    private Obj3DPart socket, chain, base, lightAlphaPlaneNoDepth;
    ResourceLocation tOn, tOff;
    private boolean onOffModel;
    private int length;
    private boolean canSwing = true;
    float baseLength, chainLength, chainFactor;

    public LampSocketSuspendedObjRender(Obj3D obj, boolean onOffModel, int length) {
        this.obj = obj;
        this.length = length;
        this.onOffModel = onOffModel;
        if (obj != null) {
            socket = obj.getPart("socket");
            chain = obj.getPart("chain");
            base = obj.getPart("base");
            lightAlphaPlaneNoDepth = obj.getPart("lightAlphaNoDepth");
            tOff = obj.getModelResourceLocation(obj.getString("tOff"));
            tOn = obj.getModelResourceLocation(obj.getString("tOn"));
            chainLength = chain.getFloat("length");
            chainFactor = chain.getFloat("factor");
            baseLength = base.getFloat("length");
        }
    }

    public LampSocketSuspendedObjRender(Obj3D obj, boolean onOffModel, int length, boolean canSwing) {
        this.canSwing = canSwing;
        this.obj = obj;
        this.length = length;
        this.onOffModel = onOffModel;
        if (obj != null) {
            socket = obj.getPart("socket");
            chain = obj.getPart("chain");
            base = obj.getPart("base");
            lightAlphaPlaneNoDepth = obj.getPart("lightAlphaNoDepth");
            tOff = obj.getModelResourceLocation(obj.getString("tOff"));
            tOn = obj.getModelResourceLocation(obj.getString("tOn"));
            chainLength = chain.getFloat("length");
            chainFactor = chain.getFloat("factor");
            baseLength = base.getFloat("length");
        }
    }

    @Override
    public void drawItem(LampSocketDescriptor descriptor, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        poseStack.translate(-1.5f, 0f, 0f);
        
        draw(poseStack, buffer, light, overlay, LRDU.Up, 0, (byte) 0, 0, 0, 0.0);
        poseStack.popPose();
    }

    @Override
    public void draw(LampSocketRender render, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        double distanceToPlayer = 0.0;
        if (render.blockEntity != null && render.blockEntity.getLevel() != null) {
             distanceToPlayer = UtilsClient.distanceFromClientPlayer(render.blockEntity);
        }
        draw(poseStack, buffer, light, overlay, render.front, render.alphaZ, render.light, render.pertuPy, render.pertuPz, distanceToPlayer);
    }

    public void draw(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, LRDU front, float alphaZ, byte light, float pertuPy, float pertuPz, double distanceToPlayer) {
        // front.rotatePoseOnX(poseStack);
        pertuPy /= length;
        pertuPz /= length;

        if (base != null) base.draw(poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(baseLength, 0, 0);

        for (int idx = 0; idx < length; idx++) {
            if (canSwing && Eln.allowSwingingLamps) {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(pertuPy));
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(pertuPz));
            }
            if (chain != null) chain.draw(poseStack, buffer, packedLight, packedOverlay);
            poseStack.translate(chainLength, 0, 0);
        }
        if (canSwing && Eln.allowSwingingLamps) {
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(pertuPy));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(pertuPz));
        }

        if (!onOffModel) {
            if (socket != null) socket.draw(poseStack, buffer, packedLight, packedOverlay);
        } else {
            // Texture swapping logic...
            if (socket != null) socket.draw(poseStack, buffer, packedLight, packedOverlay);
        }
        
        poseStack.popPose();
    }
}

package mods.eln.sixnode.electricalgatesource;

import mods.eln.misc.Obj3D;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.misc.UtilsClient;
import mods.eln.sixnode.electricalgatesource.ElectricalGateSourceDescriptor.ObjType;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.awt.*;

public class ElectricalGateSourceRenderObj {

    private Obj3DPart rot;
    private Obj3DPart main;
    private Obj3D obj;
    private Obj3DPart lever;
    private Obj3DPart led;
    private Obj3DPart halo;
    ObjType objType;
    float leverTx;

    private float rotAlphaOn, rotAlphaOff;
    public float speed;

    public ElectricalGateSourceRenderObj(Obj3D obj) {
        this.obj = obj;
        if (obj != null) {
            main = obj.getPart("main");

            if (obj.getString("type").equals("pot")) {
                objType = ObjType.Pot;
                rot = obj.getPart("rot");
                if (rot != null) {
                    rotAlphaOff = rot.getFloat("alphaOff");
                    rotAlphaOn = rot.getFloat("alphaOn");
                    speed = rot.getFloat("speed");
                }
            }

            if (obj.getString("type").equals("button")) {
                lever = obj.getPart("button");
                led = obj.getPart("led");
                halo = obj.getPart("halo");

                objType = ObjType.Button;
                if (lever != null) {
                    speed = lever.getFloat("speed");
                    leverTx = lever.getFloat("tx");
                }
            }
        }
    }

    public void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, float factor, float distance, BlockEntity e) {
        switch (objType) {
            case Button:
                if (main != null) main.draw(poseStack, buffer, light, overlay);

                poseStack.pushPose();
                poseStack.translate(leverTx * factor, 0f, 0f);
                if (lever != null) lever.draw(poseStack, buffer, light, overlay);
                poseStack.popPose();

                // UtilsClient.ledOnOffColor(factor > 0.5f);
                // UtilsClient.INSTANCE.disableLight();
                int color = factor > 0.5f ? 0xFF00B200 : 0xFFB20000;
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                
                if (led != null) UtilsClient.drawLight(led, poseStack, buffer, light, overlay, r, g, b, 1f);
                // UtilsClient.INSTANCE.enableBlend();

                if (halo != null) {
                    if (e == null)
                        UtilsClient.drawLight(halo, poseStack, buffer, light, overlay, r, g, b, 1f);
                    else {
                        // Color c = UtilsClient.ledOnOffColorC(factor > 0.5f);
                        // UtilsClient.drawHaloNoLightSetup(halo, c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, e, false);
                        // TODO: Implement halo drawing with PoseStack if needed, or just draw as light for now
                        UtilsClient.drawLight(halo, poseStack, buffer, light, overlay, r, g, b, 1f);
                    }
                }

                // UtilsClient.INSTANCE.disableBlend();
                // UtilsClient.INSTANCE.enableLight();

                break;
            case Pot:
                if (main != null) main.draw(poseStack, buffer, light, overlay);
                if (rot != null) {
                    poseStack.pushPose();
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(factor * (rotAlphaOn - rotAlphaOff) + rotAlphaOff));
                    rot.draw(poseStack, buffer, light, overlay);
                    poseStack.popPose();
                }
                break;
        }
    }
}

package mods.eln.sixnode.batterycharger;

import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Coordinate;
import net.minecraft.core.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;

public class BatteryChargerRender extends SixNodeElementRender {

    BatteryChargerDescriptor descriptor;

    Coordinate coord;
    boolean[] charged = new boolean[]{false, false, false, false};
    boolean[] batteryPresence = new boolean[]{false, false, false, false};

    float alpha = 0;

    ItemStack[] stacks = new ItemStack[4];
    boolean powerOn;
    private float voltage;

    public BatteryChargerRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, mods.eln.misc.Direction.fromMCDirection(side), descriptor);
        this.descriptor = (BatteryChargerDescriptor) descriptor;

        coord = new Coordinate(tileEntity);
    }

    @Override
    public void draw() {
        super.draw();
        PoseStack poseStack = getCurrentPoseStack();
        if (poseStack == null) return;
        MultiBufferSource buffer = getCurrentBuffer();
        if (buffer == null) return;
        int light = getCurrentLight();
        int overlay = getCurrentOverlay();

        poseStack.pushPose();

        drawPowerPin(poseStack, buffer, light, overlay, new float[]{(float)descriptor.pinDistance, (float)descriptor.pinDistance, (float)descriptor.pinDistance, (float)descriptor.pinDistance, (float)descriptor.pinDistance, (float)descriptor.pinDistance});

        if (side.isY()) {
            front.right().rotatePoseOnX(poseStack);
        }

        drawItemStack(poseStack, buffer, light, overlay, stacks[0], 0.1875, 0.15625, 0.15625, alpha, 0.2f);
        drawItemStack(poseStack, buffer, light, overlay, stacks[1], 0.1875, 0.15625, -0.15625, alpha, 0.2f);
        drawItemStack(poseStack, buffer, light, overlay, stacks[2], 0.1875, -0.15625, 0.15625, alpha, 0.2f);
        drawItemStack(poseStack, buffer, light, overlay, stacks[3], 0.1875, -0.15625, -0.15625, alpha, 0.2f);

        descriptor.draw(poseStack, buffer, light, overlay, batteryPresence, charged);
        
        poseStack.popPose();
    }

    @Override
    public void refresh(float deltaT) {
        alpha += 90 * deltaT;
        if (alpha > 360) alpha -= 360;
    }

    public void drawItemStack(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, ItemStack stack, double x, double y, double z, float roty, float scale) {
        if (stack == null || stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate((float) x, (float) y, (float) z);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(roty));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0f, -0.25f, 0.0f);
        
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poseStack, buffer, blockEntity.getLevel(), 0);
        
        poseStack.popPose();
    }

    @Nullable
    @Override
    public CableRenderDescriptor getCableRender(@NotNull LRDU lrdu) {
        return descriptor.cable.render;
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull mods.eln.misc.Direction side, @NotNull Player player) {
        return new BatteryChargerGui(this, player, inventory);
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            powerOn = stream.readBoolean();
            voltage = stream.readFloat();

            for (int idx = 0; idx < 4; idx++) {
                stacks[idx] = Utils.unserializeItemStack(stream);
            }

            byte temp = stream.readByte();
            for (int idx = 0; idx < 4; idx++) {
                charged[idx] = (temp & 1) != 0;
                temp = (byte) (temp >> 1);
            }
            temp = stream.readByte();
            for (int idx = 0; idx < 4; idx++) {
                batteryPresence[idx] = (temp & 1) != 0;
                temp = (byte) (temp >> 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SixNodeElementInventory inventory = new SixNodeElementInventory(5, 64, this);
}

package mods.eln.sixnode.resistor;

import mods.eln.cable.CableRenderType;
import mods.eln.misc.Direction;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.DataInputStream;
import java.io.IOException;

public class ResistorRender extends SixNodeElementRender {

    public ResistorDescriptor descriptor;
    SixNodeElementInventory inventory = new SixNodeElementInventory(2, 64, this);
    private CableRenderType renderPreProcess;

    private float wiperPos = 0;

    public ResistorRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (ResistorDescriptor) descriptor;
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            if (descriptor.isRheostat) wiperPos = stream.readFloat();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
        front.rotatePoseOnX(poseStack);
        descriptor.draw(poseStack, buffer, light, overlay, wiperPos);
        poseStack.popPose();
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull Direction side, @NotNull Player player) {
        return new ResistorGui(player, inventory, this);
    }
}

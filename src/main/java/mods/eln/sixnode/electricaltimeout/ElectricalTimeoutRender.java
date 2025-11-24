package mods.eln.sixnode.electricaltimeout;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.node.six.SixNodeDescriptor;
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

public class ElectricalTimeoutRender extends SixNodeElementRender {

    ElectricalTimeoutDescriptor descriptor;
    long time;

    float timeoutValue = 0, timeoutCounter = 0;
    boolean inputState;

    public ElectricalTimeoutRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (ElectricalTimeoutDescriptor) descriptor;
        time = System.currentTimeMillis();
    }

    //PhysicalInterpolator interpolator = new PhysicalInterpolator(0.2f, 2.0f, 1.5f, 0.2f);

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
        front.rotatePoseOnX(poseStack);

        descriptor.draw(poseStack, buffer, light, overlay, timeoutCounter / timeoutValue);
        poseStack.popPose();
    }

    @Override
    public void refresh(float deltaT) {
        if (!inputState) {
            timeoutCounter -= deltaT;
            if (timeoutCounter < 0f) timeoutCounter = 0f;
        }
    }

    @Override
    public boolean cameraDrawOptimisation() {
        return false;
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            timeoutValue = stream.readFloat();
            timeoutCounter = stream.readFloat();
            inputState = stream.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CableRenderDescriptor getCableRender(LRDU lrdu) {
        return Eln.instance.signalCableDescriptor.render;
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull Direction side, @NotNull Player player) {
        return new ElectricalTimeoutGui(player, this);
    }
}

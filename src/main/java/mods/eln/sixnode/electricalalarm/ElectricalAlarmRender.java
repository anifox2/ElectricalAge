package mods.eln.sixnode.electricalalarm;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import net.minecraft.core.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.RcInterpolator;
import mods.eln.misc.Utils;
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

public class ElectricalAlarmRender extends SixNodeElementRender {

    ElectricalAlarmDescriptor descriptor;

    LRDU front;

    RcInterpolator interpol = new RcInterpolator(0.4f);

    float rotAlpha = 0;
    boolean warm = false;
    boolean mute = false;

    public ElectricalAlarmRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, mods.eln.misc.Direction.fromMCDirection(side), descriptor);
        this.descriptor = (ElectricalAlarmDescriptor) descriptor;
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

        if (side.isY()) {
            front.right().rotatePoseOnX(poseStack);
            drawSignalPin(poseStack, buffer, LRDU.Down, descriptor.pinDistance);
        } else {
            drawSignalPin(poseStack, buffer, front, descriptor.pinDistance);
        }
        descriptor.draw(poseStack, buffer, light, overlay, warm, rotAlpha);
        
        poseStack.popPose();
    }

    @Override
    public void refresh(float deltaT) {
        interpol.setTarget(warm ? descriptor.rotSpeed : 0f);
        interpol.step(deltaT);

        rotAlpha += interpol.get() * deltaT;
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            Byte b;
            b = stream.readByte();
            front = LRDU.fromInt((b >> 4) & 3);
            warm = (b & 1) != 0 ? true : false;
            mute = stream.readBoolean();
            Utils.println("WARM : " + warm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public CableRenderDescriptor getCableRender(@NotNull LRDU lrdu) {
        return Eln.instance.signalCableDescriptor.render;
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull mods.eln.misc.Direction side, @NotNull Player player) {
        return new ElectricalAlarmGui(player, this);
    }
}

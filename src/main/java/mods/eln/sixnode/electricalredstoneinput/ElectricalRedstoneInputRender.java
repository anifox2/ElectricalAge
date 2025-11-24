package mods.eln.sixnode.electricalredstoneinput;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.DataInputStream;
import java.io.IOException;

public class ElectricalRedstoneInputRender extends SixNodeElementRender {

    ElectricalRedstoneInputDescriptor descriptor;

    byte redLevel;

    public ElectricalRedstoneInputRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (ElectricalRedstoneInputDescriptor) descriptor;
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

        drawSignalPin(poseStack, buffer, light, overlay, front.right(), descriptor.pinDistance);

        if (side.isY()) {
            front.right().rotatePoseOnX(poseStack);
        } else {
            LRDU.Down.rotatePoseOnX(poseStack);
        }

        descriptor.draw(poseStack, buffer, light, overlay, redLevel);
        
        poseStack.popPose();
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            redLevel = stream.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CableRenderDescriptor getCableRender(LRDU lrdu) {
        return Eln.instance.signalCableDescriptor.render;
    }
}

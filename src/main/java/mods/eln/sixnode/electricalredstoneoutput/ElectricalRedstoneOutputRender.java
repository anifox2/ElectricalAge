package mods.eln.sixnode.electricalredstoneoutput;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import net.minecraft.core.Direction;
import mods.eln.misc.LRDU;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.DataInputStream;
import java.io.IOException;

public class ElectricalRedstoneOutputRender extends SixNodeElementRender {

    ElectricalRedstoneOutputDescriptor descriptor;

    float factor;
    float factorFiltered = 0;

    int redOutput;

    public ElectricalRedstoneOutputRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, mods.eln.misc.Direction.fromMCDirection(side), descriptor);
        this.descriptor = (ElectricalRedstoneOutputDescriptor) descriptor;
    }

    @Override
    public void draw() {
        super.draw();
        PoseStack poseStack = getCurrentPoseStack();
        if (poseStack == null) return;
        MultiBufferSource buffer = getCurrentBuffer();
        if (buffer == null) return;
        int combinedLight = getCurrentLight();
        int combinedOverlay = getCurrentOverlay();

        drawSignalPin(poseStack, buffer, combinedLight, combinedOverlay, front.right(), descriptor.pinDistance);

        descriptor.draw(poseStack, buffer, combinedLight, combinedOverlay, redOutput);
    }

    @Override
    public int isProvidingWeakPower(mods.eln.misc.Direction side) {
        return redOutput;
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            redOutput = stream.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CableRenderDescriptor getCableRender(LRDU lrdu) {
        return Eln.instance.signalCableDescriptor.render;
    }
}

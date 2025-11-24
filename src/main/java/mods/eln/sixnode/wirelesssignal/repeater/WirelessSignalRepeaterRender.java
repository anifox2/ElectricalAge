package mods.eln.sixnode.wirelesssignal.repeater;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public class WirelessSignalRepeaterRender extends SixNodeElementRender {

    WirelessSignalRepeaterDescriptor descriptor;

    public WirelessSignalRepeaterRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (WirelessSignalRepeaterDescriptor) descriptor;
    }

    @Override
    public CableRenderDescriptor getCableRender(LRDU lrdu) {
        return null; //Eln.instance.signalCableDescriptor.render;
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

        front.rotatePoseOnX(poseStack);
        descriptor.draw(poseStack, buffer, combinedLight, combinedOverlay);
    }
}

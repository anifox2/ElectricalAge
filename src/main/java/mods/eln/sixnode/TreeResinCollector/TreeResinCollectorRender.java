package mods.eln.sixnode.treeresincollector;

import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.DataInputStream;
import java.io.IOException;

public class TreeResinCollectorRender extends SixNodeElementRender {

    TreeResinCollectorDescriptor descriptor;

    float stock;

    public TreeResinCollectorRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (TreeResinCollectorDescriptor) descriptor;
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
        LRDU.Down.rotatePoseOnX(poseStack);
        descriptor.draw(poseStack, buffer, light, overlay, stock);
        poseStack.popPose();
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            stock = stream.readFloat();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

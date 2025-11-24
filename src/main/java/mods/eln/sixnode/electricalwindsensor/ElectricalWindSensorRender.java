package mods.eln.sixnode.electricalwindsensor;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.RcInterpolator;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.DataInputStream;
import java.io.IOException;

public class ElectricalWindSensorRender extends SixNodeElementRender {

    ElectricalWindSensorDescriptor descriptor;

    float alpha = 0;
    float wind = 0;

    RcInterpolator windFilter = new RcInterpolator(5);

    public ElectricalWindSensorRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (ElectricalWindSensorDescriptor) descriptor;
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

        drawSignalPin(poseStack, buffer, front.right(), new float[]{2, 2, 2, 2});

        descriptor.draw(poseStack, buffer, light, overlay, alpha);
    }

    @Override
    public void refresh(float deltaT) {
        windFilter.step(deltaT);
        alpha += windFilter.get() * deltaT * 20;
        if (alpha > 360)
            alpha -= 360;
    }

    @Override
    public CableRenderDescriptor getCableRender(LRDU lrdu) {
        return Eln.instance.signalCableDescriptor.render;
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            wind = stream.readFloat();
            windFilter.setTarget(wind);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

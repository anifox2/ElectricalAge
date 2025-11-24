package mods.eln.sixnode.electricalweathersensor;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectricalWeatherSensorRender extends SixNodeElementRender {

    ElectricalWeatherSensorDescriptor descriptor;

    public ElectricalWeatherSensorRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (ElectricalWeatherSensorDescriptor) descriptor;
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

        drawSignalPin(poseStack, buffer, front.right(), descriptor.pinDistance);

        descriptor.draw(poseStack, buffer, light, overlay);
    }

    @Nullable
    @Override
    public CableRenderDescriptor getCableRender(@NotNull LRDU lrdu) {
        return Eln.instance.signalCableDescriptor.render;
    }
}

package mods.eln.sixnode.electricalentitysensor;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.item.EntitySensorFilterDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;

public class ElectricalEntitySensorRender extends SixNodeElementRender {

    ElectricalEntitySensorDescriptor descriptor;

    boolean state = false;
    EntitySensorFilterDescriptor filter = null;

    SixNodeElementInventory inventory = new SixNodeElementInventory(1, 64, this);

    public ElectricalEntitySensorRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (ElectricalEntitySensorDescriptor) descriptor;
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

        descriptor.draw(poseStack, buffer, light, overlay, state, filter);
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            state = stream.readBoolean();
            ItemStack filterStack = Utils.unserialiseItemStack(stream);
            filter = (EntitySensorFilterDescriptor) EntitySensorFilterDescriptor.getDescriptor(filterStack);
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
    public Screen newGuiDraw(@NotNull Direction side, @NotNull Player player) {
        return new ElectricalEntitySensorGui(player, inventory, this);
    }
}

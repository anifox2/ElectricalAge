package mods.eln.sixnode.groundcable;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.DataInputStream;
import java.io.IOException;

public class GroundCableRender extends SixNodeElementRender {

    GroundCableDescriptor descriptor;

    //double voltage = 0,current = 0;
    int color = 0;

    CableRenderDescriptor cableRender;

    SixNodeElementInventory inventory = new SixNodeElementInventory(1, 64, this);

    public GroundCableRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (GroundCableDescriptor) descriptor;
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
            front.rotatePoseOnX(poseStack);
        }

        descriptor.draw(poseStack, buffer, light, overlay);
        
        poseStack.popPose();
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            Byte b;
            b = stream.readByte();

            color = (b >> 4) & 0xF;

            ItemStack cableStack = Utils.unserialiseItemStack(stream);
            if (!cableStack.isEmpty()) {
                ElectricalCableDescriptor desc = (ElectricalCableDescriptor) ElectricalCableDescriptor.getDescriptor(cableStack);

                if (desc == null)
                    cableRender = Eln.instance.lowVoltageCableDescriptor.render;
                else
                    cableRender = desc.render;
            } else {
                cableRender = Eln.instance.lowVoltageCableDescriptor.render;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CableRenderDescriptor getCableRender(LRDU lrdu) {
        return cableRender;
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull Direction side, @NotNull Player player) {
        return new GroundCableGui(player, inventory, this);
    }
}

package mods.eln.sixnode.lampsupply;

import mods.eln.cable.CableRenderDescriptor;
import mods.eln.cable.CableRenderType;
import mods.eln.misc.Coordinate;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.PhysicalInterpolator;
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
import java.util.ArrayList;

public class LampSupplyRender extends SixNodeElementRender {

    LampSupplyDescriptor descriptor;

    Coordinate coord;
    PhysicalInterpolator interpolator;

    public ArrayList<LampSupplyElement.Entry> entries = new ArrayList<LampSupplyElement.Entry>();


    CableRenderDescriptor cableRender;

    SixNodeElementInventory inventory = new SixNodeElementInventory(1, 64, this);

    public LampSupplyRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (LampSupplyDescriptor) descriptor;
        interpolator = new PhysicalInterpolator(0.4f, 8.0f, 0.9f, 0.2f);
        coord = new Coordinate(tileEntity);
        for (int i = 0; i < ((LampSupplyDescriptor) descriptor).channelCount; i++) {
            entries.add(new LampSupplyElement.Entry("", "", 2));
        }
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

        float[] pinDistances = new float[]{4.98f, 4.98f, 5.98f, 5.98f};

        if (side.isY()) {
            drawPowerPin(poseStack, buffer, light, overlay, front.rotate4PinDistances(pinDistances));
            front.rotatePoseOnX(poseStack);
        } else {
            drawPowerPin(poseStack, buffer, light, overlay, pinDistances);
            LRDU.Down.rotatePoseOnX(poseStack);
        }
        descriptor.draw(poseStack, buffer, light, overlay, interpolator.get());
        
        poseStack.popPose();
    }

    @Override
    public void refresh(float deltaT) {
        if (!Utils.isPlayerAround(blockEntity.getLevel(), coord.getAxisAlignedBB(0)))
            interpolator.setTarget(0f);
        else
            interpolator.setTarget(1f);

        interpolator.step(deltaT);
    }

    @Nullable
    @Override
    public CableRenderDescriptor getCableRender(@NotNull LRDU lrdu) {
        return cableRender;
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull Direction side, @NotNull Player player) {
        return new LampSupplyGui(this, player, inventory);
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            for (LampSupplyElement.Entry e : entries) {
                e.powerChannel = stream.readUTF();
                e.wirelessChannel = stream.readUTF();
                e.aggregator = stream.readChar();
            }

            ItemStack cableStack = Utils.unserialiseItemStack(stream);
            if (cableStack != null) {
                ElectricalCableDescriptor desc = (ElectricalCableDescriptor) ElectricalCableDescriptor.getDescriptor(cableStack);
                cableRender = desc.render;
            } else {
                cableRender = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void newConnectionType(CableRenderType connectionType) {
        for (int idx = 0; idx < 4; idx++) {
            connectionType.startAt[idx] = 5 / 16f;
        }
    }
}

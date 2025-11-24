package mods.eln.sixnode.energymeter;

import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.misc.UtilsClient;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor;
import mods.eln.sixnode.energymeter.EnergyMeterElement.Mod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.DataInputStream;
import java.io.IOException;

public class EnergyMeterRender extends SixNodeElementRender {

    SixNodeElementInventory inventory = new SixNodeElementInventory(1, 64, this);
    EnergyMeterDescriptor descriptor;

    double timerCouter, energyStack;
    boolean switchState;
    String password;
    Mod mod;

    int energyUnit, timeUnit;

    CableRenderDescriptor cableRender;

    double power;
    double error;
    double serverPowerIdTimer = EnergyMeterElement.SlowProcess.publishTimeoutReset * 34;

    public EnergyMeterRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (EnergyMeterDescriptor) descriptor;

		/*for (int idx = 0; idx < energyRc.length; idx++) {
            energyRc[idx] = new RcInterpolator(0.2f);
		}*/
    }

    //RcInterpolator[] energyRc = new RcInterpolator[7];

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

        float[] pinDistances = descriptor.pinDistance;
        if (side.isY()) {
            pinDistances = front.rotate4PinDistances(pinDistances);
            front.left().rotatePoseOnX(poseStack);
        }

        descriptor.draw(poseStack, buffer, light, overlay, energyStack / Math.pow(10, energyUnit * 3 - 1), timerCouter / (timeUnit == 0 ? 360 : 8640),
            energyUnit, timeUnit,
            UtilsClient.distanceFromClientPlayer(getTileEntity()) < 20);

        poseStack.popPose();
    }

    @Override
    public void refresh(float deltaT) {
        double errorComp = error * 1 * deltaT;
        energyStack += power * deltaT + errorComp;
        error -= errorComp;

		/*double stack = energyStack;
		for (int idx = 0; idx < energyRc.length; idx++) {

			energyRc[idx].setTarget((float) ((stack) % 10));
			energyRc[idx].step(deltaT);
			stack /= 10.0;
		}*/

        timerCouter += deltaT * 72;
        serverPowerIdTimer += deltaT;
    }

    @Nullable
    @Override
    public CableRenderDescriptor getCableRender(@NotNull LRDU lrdu) {
        return cableRender;
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);

        try {
            switchState = stream.readBoolean();
            password = stream.readUTF();
            mod = Mod.valueOf(stream.readUTF());
            timerCouter = stream.readDouble();
            // energyStack = stream.readDouble();
            ItemStack itemStack = Utils.unserialiseItemStack(stream);
            energyUnit = stream.readByte();
            timeUnit = stream.readByte();
            if (itemStack != null) {
                ElectricalCableDescriptor desc = (ElectricalCableDescriptor) ElectricalCableDescriptor.getDescriptor(itemStack, ElectricalCableDescriptor.class);
                if (desc == null)
                    cableRender = null;
                else
                    cableRender = desc.render;
            } else {
                cableRender = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull Direction side, @NotNull Player player) {
        return new EnergyMeterGui(player, inventory, this);
    }

    @Override
    public void serverPacketUnserialize(DataInputStream stream) throws IOException {
        super.serverPacketUnserialize(stream);

        switch (stream.readByte()) {
            case EnergyMeterElement.serverPowerId:
                if (serverPowerIdTimer > EnergyMeterElement.SlowProcess.publishTimeoutReset * 3) {
                    energyStack = stream.readDouble();
                    error = 0;
                } else {
                    error = stream.readDouble() - energyStack;
                }
                power = stream.readDouble();
                serverPowerIdTimer = 0;
                break;
            default:
                break;
        }
    }
}

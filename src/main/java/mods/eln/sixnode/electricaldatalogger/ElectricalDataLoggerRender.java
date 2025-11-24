package mods.eln.sixnode.electricaldatalogger;

import mods.eln.Eln;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.DataInputStream;
import java.io.IOException;

public class ElectricalDataLoggerRender extends SixNodeElementRender {

    SixNodeElementInventory inventory = new SixNodeElementInventory(2, 64, this);
    ElectricalDataLoggerDescriptor descriptor;
    long time;

    public boolean pause;
    public byte color = 15;

    DataLogs log = new DataLogs(ElectricalDataLoggerElement.logsSizeMax);
    boolean waitFistSync = true;

    public ElectricalDataLoggerRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, side, descriptor);
        this.descriptor = (ElectricalDataLoggerDescriptor) descriptor;
        time = System.currentTimeMillis();
        clientSend(ElectricalDataLoggerElement.newClientId);
    }

    @Override
    public CableRenderDescriptor getCableRender(LRDU lrdu) {
        return Eln.instance.signalCableDescriptor.render;
    }

    @Override
    public void draw() {
        super.draw();
        if (this.blockEntity == null) return;
        PoseStack poseStack = getCurrentPoseStack();
        if (poseStack == null) return;
        MultiBufferSource buffer = getCurrentBuffer();
        if (buffer == null) return;
        int light = getCurrentLight();
        int overlay = getCurrentOverlay();

        descriptor.draw(poseStack, buffer, light, overlay, this.log, side, front, this.blockEntity.getBlockPos().getX(), this.blockEntity.getBlockPos().getZ(), color);
    }

	/*
    @Override
	public CableRenderDescriptor getCableRender(LRDU lrdu) {
		return descriptor.cableRender;
	}
	*/

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            log.unitType = stream.readByte();
            pause = stream.readBoolean();
            log.samplingPeriod = stream.readFloat();
            log.maxValue = stream.readFloat();
            log.minValue = stream.readFloat();
            color = stream.readByte();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serverPacketUnserialize(DataInputStream stream) throws IOException {
        byte header = stream.readByte();

        switch (header) {
            case ElectricalDataLoggerElement.toClientLogsAdd:
            case ElectricalDataLoggerElement.toClientLogsClear:
                if (header == ElectricalDataLoggerElement.toClientLogsClear) {
                    log.reset();
                    waitFistSync = false;
                }
                int size = stream.available();
                while (size != 0) {
                    size--;
                    log.write(stream.readByte());
                }
                //	Utils.println(log);
                break;
        }
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull Direction side, @NotNull Player player) {
        return new ElectricalDataLoggerGui(player, inventory, this);
    }
}

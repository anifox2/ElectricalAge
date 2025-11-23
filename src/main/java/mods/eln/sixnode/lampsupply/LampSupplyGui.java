package mods.eln.sixnode.lampsupply;

import mods.eln.gui.GuiButtonEln;
import mods.eln.gui.GuiContainerEln;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.GuiTextFieldEln;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class LampSupplyGui extends GuiContainerEln<LampSupplyContainer> {

    private LampSupplyRender render;

    private List<AggregatorBtInfo> buttons = new ArrayList<>();

    static class AggregatorBtInfo {
        GuiButtonEln btn;
        int channel;
        byte id;

        public AggregatorBtInfo(GuiButtonEln btn, int channel, byte id) {
            this.btn = btn;
            this.channel = channel;
            this.id = id;
        }
    }

    public LampSupplyGui(LampSupplyRender render, Player player, Container inventory) {
        super(new LampSupplyContainer(player, inventory), player.getInventory(), Component.literal("Lamp Supply"));
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttons.clear();
        int y = 6;

        int x;
        for (int id = 0; id < render.descriptor.channelCount; id++) {
            x = 6;

            LampSupplyElement.Entry e = render.entries.get(id);
            GuiTextFieldEln powerChannel = newGuiTextField(x, y, 101);
            x += powerChannel.getWidth() + 12;
            powerChannel.setText(e.powerChannel);
            powerChannel.setComment(new String[]{tr("Power channel name")});
            int finalId = id;
            powerChannel.setObserver((tf, val) -> {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    DataOutputStream stream = new DataOutputStream(bos);
                    render.preparePacketForServer(stream);
                    stream.writeByte(LampSupplyElement.setPowerName);
                    stream.writeByte(finalId);
                    stream.writeUTF(val);
                    render.sendPacketToServer(bos);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            GuiTextFieldEln wirelessChannel = newGuiTextField(x, y, 101);
            x += wirelessChannel.getWidth() + 12;
            wirelessChannel.setText(e.wirelessChannel);
            wirelessChannel.setComment(new String[]{tr("Wireless channel name")});
            wirelessChannel.setObserver((tf, val) -> {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    DataOutputStream stream = new DataOutputStream(bos);
                    render.preparePacketForServer(stream);
                    stream.writeByte(LampSupplyElement.setWirelessName);
                    stream.writeByte(finalId);
                    stream.writeUTF(val);
                    render.sendPacketToServer(bos);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            y += wirelessChannel.getHeight() + 2;
            x = 6;
            int w = 68;
            
            GuiButtonEln buttonBigger = newGuiButton(x, y, w, tr("Biggest"), (b) -> onAggregatorClick(finalId, (byte) 0));
            buttons.add(new AggregatorBtInfo(buttonBigger, finalId, (byte) 0));
            x += 2 + w;
            
            GuiButtonEln buttonSmaller = newGuiButton(x, y, w, tr("Smallest"), (b) -> onAggregatorClick(finalId, (byte) 1));
            buttons.add(new AggregatorBtInfo(buttonSmaller, finalId, (byte) 1));
            x += 2 + w;
            
            GuiButtonEln buttonToogle = newGuiButton(x, y, w, tr("Toggle"), (b) -> onAggregatorClick(finalId, (byte) 2));
            buttons.add(new AggregatorBtInfo(buttonToogle, finalId, (byte) 2));
            x += 2 + w;

            buttonBigger.setComment(tr("Uses the biggest\nvalue on the channel.").split("\n"));
            buttonSmaller.setComment(tr("Uses the smallest\nvalue on the channel.").split("\n"));
            buttonToogle.setComment(tr("Toggles the output each time\nan emitter's value rises.\nUseful to allow multiple buttons\nto control the same light.").split("\n"));
            
            y += buttonToogle.getHeight() + 6;
        }
    }

    void onAggregatorClick(int channel, byte id) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(bos);

            render.preparePacketForServer(stream);

            stream.writeByte(LampSupplyElement.setSelectedAggregator);
            stream.writeByte(channel);
            stream.writeByte(id);

            render.sendPacketToServer(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
        for (AggregatorBtInfo info : buttons) {
            info.btn.setEnabled(render.entries.get(info.channel).aggregator != info.id);
        }
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 220, 205, 8, 125);
    }
}

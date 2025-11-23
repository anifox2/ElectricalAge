package mods.eln.sixnode.wirelesssignal.rx;

import mods.eln.gui.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import static mods.eln.i18n.I18N.tr;

public class WirelessSignalRxGui extends ScreenEln implements GuiTextFieldEln.GuiTextFieldElnObserver {

    GuiTextFieldEln channel;
    private WirelessSignalRxRender render;

    GuiButtonEln buttonBigger, buttonSmaller, buttonToogle;

    public WirelessSignalRxGui(WirelessSignalRxRender render) {
        super(Component.literal("Wireless Rx"));
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();
        channel = newGuiTextField(6, 6, 220);
        channel.setText(render.channel);
        channel.setObserver(this);
        channel.setComment(new String[]{tr("Specify the channel")});

        int w = 72;
        int x = 6;
        int y = 6 + 12 + 4;
        
        buttonBigger = newGuiButton(x, y, w, tr("Biggest"), (btn) -> {
            render.clientSetByte(WirelessSignalRxElement.setSelectedAggregator, (byte) 0);
        });
        x += 2 + w;
        
        buttonSmaller = newGuiButton(x, y, w, tr("Smallest"), (btn) -> {
            render.clientSetByte(WirelessSignalRxElement.setSelectedAggregator, (byte) 1);
        });
        x += 2 + w;
        
        buttonToogle = newGuiButton(x, y, w, tr("Toggle"), (btn) -> {
            render.clientSetByte(WirelessSignalRxElement.setSelectedAggregator, (byte) 2);
        });

        buttonBigger.setComment(0, tr("Uses the biggest\nvalue on the channel."));
        buttonSmaller.setComment(0, tr("Uses the smallest\nvalue on the channel."));
        buttonToogle.setComment(0, tr("Toggles the output each time\nan emitter's value rises.\nUseful to allow multiple buttons\nto control the same light."));
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        if (render.connection)
            channel.setComment(new String[]{"\u00a72" + tr("Connected")});
        else
            channel.setComment(new String[]{"\u00a74" + tr("Not connected")});

        buttonBigger.setEnabled(render.selectedAggregator != 0);
        buttonSmaller.setEnabled(render.selectedAggregator != 1);
        buttonToogle.setEnabled(render.selectedAggregator != 2);

        super.preDraw(guiGraphics, f, x, y);
    }

    @Override
    public void textFieldNewValue(GuiTextFieldEln textField, String value) {
        if (textField == channel) {
            render.clientSetString(WirelessSignalRxElement.setChannelId, value);
        }
    }
}

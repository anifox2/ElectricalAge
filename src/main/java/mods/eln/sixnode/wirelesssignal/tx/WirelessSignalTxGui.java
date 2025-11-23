package mods.eln.sixnode.wirelesssignal.tx;

import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.ScreenEln;
import mods.eln.gui.GuiTextFieldEln;
import mods.eln.gui.IGuiObject;

import static mods.eln.i18n.I18N.tr;

public class WirelessSignalTxGui extends ScreenEln implements GuiTextFieldEln.GuiTextFieldElnObserver {

    GuiTextFieldEln channel;
    private WirelessSignalTxRender render;

    public WirelessSignalTxGui(WirelessSignalTxRender render) {
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();
        channel = newGuiTextField(6, 6, 220);
        channel.setText(render.channel);
        channel.setComment(new String[]{tr("Specify the channel")});
        channel.setObserver(this);
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 220 + 12, 12 + 12);
    }

    @Override
    public void textFieldNewValue(GuiTextFieldEln textField, String value) {
        if (textField == channel) {
            render.clientSetString(WirelessSignalTxElement.setChannelId, value);
        }
    }
}

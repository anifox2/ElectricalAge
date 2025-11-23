package mods.eln.sixnode.electricaltimeout;

import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.ScreenEln;
import mods.eln.gui.GuiTextFieldEln;
import mods.eln.gui.GuiButtonEln;
import mods.eln.gui.IGuiObject;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.text.NumberFormat;
import java.text.ParseException;

import static mods.eln.i18n.I18N.tr;

public class ElectricalTimeoutGui extends ScreenEln {

    GuiButtonEln set, reset;
    GuiTextFieldEln timeoutValue;
    ElectricalTimeoutRender render;

    public ElectricalTimeoutGui(Player player, ElectricalTimeoutRender render) {
        super(Component.empty());
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();

        reset = newGuiButton(6, 6, 50, tr("Reset"), (btn) -> {
            render.clientSend(ElectricalTimeoutElement.resetId);
        });
        
        set = newGuiButton(6, 6 + 20 + 4, 50, tr("Set"), (btn) -> {
            render.clientSend(ElectricalTimeoutElement.setId);
        });

        timeoutValue = newGuiTextField(6, 6 + 20 * 2 + 4 * 2, 50);

        timeoutValue.setText(String.valueOf(render.timeoutValue));

        timeoutValue.setComment(tr("The time interval the\noutput is kept high.").split("\n"));
        
        timeoutValue.setObserver((tf, val) -> {
            try {
                float value = NumberFormat.getInstance().parse(val).floatValue();
                render.clientSetFloat(ElectricalTimeoutElement.setTimeOutValueId, value);
            } catch (ParseException e) {
            }
        });
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 50 + 12, 6 + 20 * 2 + 4 * 2 + 12 + 6);
    }
}

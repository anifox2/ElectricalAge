package mods.eln.sixnode.electricalbreaker;

import mods.eln.gui.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;

import java.text.NumberFormat;
import java.text.ParseException;

import static mods.eln.i18n.I18N.tr;

public class ElectricalBreakerGui extends GuiContainerEln<ElectricalBreakerContainer> implements GuiTextFieldEln.GuiTextFieldElnObserver {

    GuiButtonEln toogleSwitch;
    GuiTextFieldEln setUmin, setUmax;
    ElectricalBreakerRender render;

    enum SelectedType {none, min, max}

    public ElectricalBreakerGui(Player player, Container inventory, ElectricalBreakerRender render) {
        super(new ElectricalBreakerContainer(player, inventory), player.getInventory(), Component.literal("Breaker"));
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();

        setUmin = newGuiTextField(12, 58 / 2 + 3, 50);
        setUmax = newGuiTextField(12, 58 / 2 - 5 - 10, 50);

        setUmin.setText(String.valueOf(render.uMin));
        setUmax.setText(String.valueOf(render.uMax));

        setUmin.setObserver(this);
        setUmax.setObserver(this);

        setUmin.setComment(new String[]{tr("Minimum voltage before cutting off")});
        setUmax.setComment(new String[]{tr("Maximum voltage before cutting off")});

        toogleSwitch = newGuiButton(72 - 2, 58 / 2 - 10, 70, tr("Toggle switch"), (btn) -> {
            render.clientToogleSwitch();
        });
    }

    @Override
    public void textFieldNewValue(GuiTextFieldEln textField, String value) {
        if (textField == setUmax) {
            try {
                render.clientSetVoltageMax(NumberFormat.getInstance().parse(value).floatValue());
            } catch (ParseException e) {
            }
        } else if (textField == setUmin) {
            try {
                render.clientSetVoltageMin(NumberFormat.getInstance().parse(value).floatValue());
            } catch (ParseException e) {
            }
        }
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
        if (!render.switchState)
            toogleSwitch.setDisplayString(tr("Switch is off"));
        else
            toogleSwitch.setDisplayString(tr("Switch is on"));
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new HelperStdContainerSmall(this);
    }
}

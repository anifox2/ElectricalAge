package mods.eln.sixnode.electricalalarm;

import mods.eln.gui.GuiButtonEln;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.ScreenEln;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import static mods.eln.i18n.I18N.tr;

public class ElectricalAlarmGui extends ScreenEln {

    GuiButtonEln toogleDefaultOutput;
    ElectricalAlarmRender render;

    public ElectricalAlarmGui(Player player, ElectricalAlarmRender render) {
        this.render = render;
    }

    @Override
    public void init() {
        super.init();

        toogleDefaultOutput = new GuiButtonEln(leftPos + 6, topPos + 32 / 2 - 10, 115, 20, tr("Toggle switch"), (b) -> {
            render.clientSend(ElectricalAlarmElement.clientSoundToggle);
        });
        addRenderableWidget(toogleDefaultOutput);
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
        if (!render.mute)
            toogleDefaultOutput.setMessage(net.minecraft.network.chat.Component.literal(tr("Sound is not muted")));
        else
            toogleDefaultOutput.setMessage(net.minecraft.network.chat.Component.literal(tr("Sound is muted")));
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 128, 32);
    }
}

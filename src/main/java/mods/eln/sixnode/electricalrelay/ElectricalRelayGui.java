package mods.eln.sixnode.electricalrelay;

import mods.eln.gui.GuiButtonEln;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.ScreenEln;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import static mods.eln.i18n.I18N.tr;

public class ElectricalRelayGui extends ScreenEln {

    GuiButtonEln toggleDefaultOutput;
    ElectricalRelayRender render;

    public ElectricalRelayGui(Player player, ElectricalRelayRender render) {
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();

        toggleDefaultOutput = new GuiButtonEln(leftPos + 6, topPos + 32 / 2 - 10, 115, 20, tr("Toggle switch"), (b) -> {
            render.clientToogleDefaultOutput();
        });
        addRenderableWidget(toggleDefaultOutput);
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
        if (render.defaultOutput)
            toggleDefaultOutput.setMessage(net.minecraft.network.chat.Component.literal(tr("Normally closed")));
        else
            toggleDefaultOutput.setMessage(net.minecraft.network.chat.Component.literal(tr("Normally open")));
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 128, 32);
    }
}

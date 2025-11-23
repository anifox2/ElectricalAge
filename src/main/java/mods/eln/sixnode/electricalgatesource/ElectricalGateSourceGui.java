package mods.eln.sixnode.electricalgatesource;

import mods.eln.Eln;
import mods.eln.gui.GuiHelper;
import mods.eln.gui.ScreenEln;
import mods.eln.gui.GuiVerticalTrackBar;
import mods.eln.gui.IGuiObject;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.GuiGraphics;
import mods.eln.gui.GuiHelperContainer;

import static mods.eln.i18n.I18N.tr;

public class ElectricalGateSourceGui extends ScreenEln {

    ElectricalGateSourceRender render;
    GuiVerticalTrackBar voltage;

    public ElectricalGateSourceGui(Player player, ElectricalGateSourceRender render) {
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();

        voltage = newGuiVerticalTrackBar(6, 6 + 2, 20, 50);
        voltage.setStepIdMax((int) 100);
        voltage.setEnable(true);
        voltage.setRange(0f, (float) Eln.SVU);

        syncVoltage();
    }

    public void syncVoltage() {
        voltage.setValue(render.voltageSyncValue);
        render.voltageSyncNew = false;
    }

    @Override
    public void guiObjectEvent(IGuiObject object) {
        super.guiObjectEvent(object);
        if (object == voltage) {
            render.clientSetFloat(ElectricalGateSourceElement.setVoltagerId, voltage.getValue());
        }
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
        if (render.voltageSyncNew) syncVoltage();
        voltage.setComment(0, tr("Output at %1$%", (int)((voltage.getValue() / Eln.SVU) * 100)));
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 12 + 20, 12 + 50 + 4);
    }
}

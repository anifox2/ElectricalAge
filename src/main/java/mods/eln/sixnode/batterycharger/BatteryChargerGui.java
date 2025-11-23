package mods.eln.sixnode.batterycharger;

import mods.eln.gui.GuiButtonEln;
import mods.eln.gui.GuiContainerEln;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.IGuiObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.network.chat.Component;

import static mods.eln.i18n.I18N.tr;

public class BatteryChargerGui extends GuiContainerEln<BatteryChargerContainer> {

    private BatteryChargerRender render;

    GuiButtonEln powerOn;

    public BatteryChargerGui(BatteryChargerRender render, Player player, Container inventory) {
        super(new BatteryChargerContainer(player, inventory), player.getInventory(), Component.literal("Battery Charger"));
        this.render = render;
    }

    @Override
    public void init() {
        super.init();
        powerOn = newGuiButton(97 + 10, 6 + 17 - 10, 40, "");
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
        if (render.powerOn) {
            powerOn.setMessage(Component.literal(tr("Is on")));
        } else {
            powerOn.setMessage(Component.literal(tr("Is off")));
        }
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 176, 166 - 40, 8, 84 - 40);
    }

    @Override
    public void guiObjectEvent(IGuiObject object) {
        if (object == powerOn) {
            render.clientSend(BatteryChargerElement.toogleCharge);
        }
        super.guiObjectEvent(object);
    }
}

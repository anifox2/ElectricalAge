package mods.eln.sixnode.resistor;

import mods.eln.gui.GuiContainerEln;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.IGuiObject;
import mods.eln.misc.Utils;
import mods.eln.node.six.SixNodeElementInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

import static mods.eln.i18n.I18N.tr;

public class ResistorGui extends GuiContainerEln<ResistorContainer> {

    ResistorRender render;
    private SixNodeElementInventory inventory;

    public ResistorGui(Player player, Container inventory, ResistorRender render) {
        super(new ResistorContainer(player, inventory), player.getInventory(), Component.literal("Resistor"));
        this.inventory = (SixNodeElementInventory) inventory;
        this.render = render;
    }

    public void initGui() {
        super.initGui();
    }

    @Override
    public void guiObjectEvent(IGuiObject object) {
        //super.guiObjectEvent(object);
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
    }

    @Override
    public void postDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        helper.drawString(guiGraphics, 8, 12, String.format(tr("Resistance: %1$s\u2126"), Utils.plotValue(render.descriptor.getRsValue(render.inventory))), 0xFF000000);
        super.postDraw(guiGraphics, f, x, y);
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 176, 166 - 54, 8, 84 - 54, null);
    }
}

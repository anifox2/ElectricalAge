package mods.eln.sixnode.groundcable;

import mods.eln.gui.GuiContainerEln;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.GuiTextFieldEln;
import mods.eln.gui.GuiButtonEln;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;

public class GroundCableGui extends GuiContainerEln {

    GuiButtonEln toogleSwitch;
    GuiTextFieldEln setUmin, setUmax;
    GroundCableRender render;

    enum SelectedType {none, min, max}

    public GroundCableGui(Player player, Container inventory, GroundCableRender render) {
        super(new GroundCableContainer(player, inventory), player.getInventory(), net.minecraft.network.chat.Component.literal("Ground Cable"));
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 176, 166 - 52, 8, 84 - 52);
    }
}

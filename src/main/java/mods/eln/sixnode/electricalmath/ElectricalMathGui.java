package mods.eln.sixnode.electricalmath;

import mods.eln.gui.GuiContainerEln;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.GuiTextFieldEln;
import mods.eln.gui.IGuiObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import static mods.eln.i18n.I18N.tr;

public class ElectricalMathGui extends GuiContainerEln implements GuiTextFieldEln.GuiTextFieldElnObserver {

    GuiTextFieldEln expression;
    ElectricalMathRender render;

    public ElectricalMathGui(Player player, Container inventory, ElectricalMathRender render) {
        super(new ElectricalMathContainer(null, player, inventory), player.getInventory(), Component.literal("Electrical Math"));
        //this.inventory = (TransparentNodeElementInventory) inventory;
        this.render = render;
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 176 + 44, 166 - 38, 8 + 44 / 2, 84 - 38);
    }

    @Override
    public void initGui() {
        super.initGui();

        expression = newGuiTextField(8, 8, 176 - 16 + 44);
        expression.setText(render.expression);
        expression.setObserver(this);
        expression.setComment(new String[]{tr("Output voltage formula"),
            tr("Inputs are") + " \u00a74A \u00a72B \u00a71C"});
    }

    @Override
    public void guiObjectEvent(IGuiObject object) {
        super.guiObjectEvent(object);
        if (object == expression) {
            render.clientSetString(ElectricalMathElement.setExpressionId, expression.getText());
        }
    }

    @Override
    public void postDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.postDraw(guiGraphics, f, x, y);
        int c;
        int redNbr = 0;
        ItemStack stack = render.inventory.getItem(ElectricalMathContainer.restoneSlotId);

        if (stack != null)
            redNbr = stack.getCount();
        if (!expression.getText().equals(render.expression)) {
            c = 0xFF404040;
            helper.drawString(guiGraphics, 8 + 44 / 2, 29, tr("Waiting for completion..."), c);
        } else if (expression.getText().equals("")) {
            c = 0xFF404040;
            helper.drawString(guiGraphics, 8 + 44 / 2, 29, tr("Equation required!"), c);
        } else if (render.equationIsValid) {
            if (redNbr >= render.redstoneRequired)
                c = 0xFF108F00;
            else
                c = 0xFFFF0000;
            helper.drawString(guiGraphics, 8 + 44 / 2, 29, tr("%1$ Redstone(s) required", render.redstoneRequired), c);
        } else {
            c = 0xFFFF0000;
            helper.drawString(guiGraphics, 8 + 44 / 2, 29, tr("Invalid equation!"), c);
        }
    }

    @Override
    public void textFieldNewValue(GuiTextFieldEln textField, String value) {
    }
}

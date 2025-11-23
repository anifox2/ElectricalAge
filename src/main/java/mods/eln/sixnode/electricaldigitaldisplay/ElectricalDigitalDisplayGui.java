package mods.eln.sixnode.electricaldigitaldisplay;

import mods.eln.gui.GuiHelper;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.ScreenEln;
import mods.eln.gui.GuiTextFieldEln;
import mods.eln.gui.IGuiObject;
import mods.eln.gui.GuiButtonEln;
import mods.eln.i18n.I18N;
import mods.eln.misc.Utils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

public class ElectricalDigitalDisplayGui extends ScreenEln {
    GuiTextFieldEln minValue, maxValue;
    GuiButtonEln validate;
    ElectricalDigitalDisplayRender render;

    public ElectricalDigitalDisplayGui(ElectricalDigitalDisplayRender render) {
        super(Component.literal("Digital Display"));
        this.render = render;
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 176, 166, 8, 84, "pal.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        minValue = newGuiTextField(8, 24, 70);
        minValue.setComment(new String[]{"Display at minimum signal input"});
        minValue.setText(String.format("%.2f", render.min));
        maxValue = newGuiTextField(8, 8, 70);
        maxValue.setComment(new String[]{"Display at maximum signal input"});
        maxValue.setText(String.format("%.2f", render.max));
        validate = newGuiButton(82, 12, 80, I18N.tr("Validate"), (btn) -> {
            validateAction();
        });
        validate.active = true;
    }

    private void validateAction() {
        try {
            NumberFormat fmt = NumberFormat.getInstance();
            float newMin = fmt.parse(minValue.getValue()).floatValue();
            float newMax = fmt.parse(maxValue.getValue()).floatValue();
            Utils.println(String.format("EDDG sending %f - %f", newMin, newMax));

            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream stream = new DataOutputStream(bos);

                render.preparePacketForServer(stream);

                stream.writeByte(ElectricalDigitalDisplayDescriptor.netSetRange);
                stream.writeFloat(newMin);
                stream.writeFloat(newMax);

                render.sendPacketToServer(bos);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } catch(ParseException e) {}
    }

    @Override
    public void guiObjectEvent(IGuiObject object) {
        //super.guiObjectEvent(object);
    }
}

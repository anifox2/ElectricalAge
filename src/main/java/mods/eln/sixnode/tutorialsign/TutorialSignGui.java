package mods.eln.sixnode.tutorialsign;

import mods.eln.gui.GuiHelper;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.ScreenEln;
import mods.eln.gui.GuiTextFieldEln;
import net.minecraft.network.chat.Component;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static mods.eln.i18n.I18N.tr;

public class TutorialSignGui extends ScreenEln implements GuiTextFieldEln.GuiTextFieldElnObserver {

    GuiTextFieldEln fileName;
    TutorialSignRender render;

    public TutorialSignGui(TutorialSignRender render) {
        super(Component.literal("Tutorial Sign"));
        this.render = render;
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 176, 166, 8, 84, "pal.png");
    }

    @Override
    public void initGui() {
        super.initGui();

        fileName = newGuiTextField(6, 6, 150);
        fileName.setText(render.baliseName);
        fileName.setObserver(this);
        fileName.setComment(new String[]{tr("Set beacon name")});
    }

    @Override
    public void textFieldNewValue(GuiTextFieldEln textField, String value) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(bos);

            render.preparePacketForServer(stream);

            stream.writeByte(TutorialSignElement.setTextFileId);
            stream.writeUTF(fileName.getText());

            render.sendPacketToServer(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

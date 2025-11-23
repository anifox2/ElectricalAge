package mods.eln.sixnode.electricaldatalogger;

import mods.eln.gui.GuiContainerEln;
import mods.eln.gui.GuiButtonEln;
import mods.eln.gui.GuiHelperContainer;
import mods.eln.gui.GuiTextFieldEln;
import mods.eln.gui.GuiTextFieldEln.GuiTextFieldElnObserver;
import mods.eln.gui.IGuiObject;
import mods.eln.misc.FC;
import mods.eln.misc.UtilsClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import org.lwjgl.opengl.GL11;

import java.text.NumberFormat;
import java.text.ParseException;

import static mods.eln.i18n.I18N.tr;

public class ElectricalDataLoggerGui extends GuiContainerEln {

    GuiButtonEln resetBt, voltageType, energyType, currentType, powerType, celsiusType, percentType, noType, config, printBt, pause;
    GuiTextFieldEln samplingPeriod, maxValue, minValue, yCursorValue;
    ElectricalDataLoggerRender render;

    enum State {display, config}

    State state = State.display;

    public ElectricalDataLoggerGui(Player player, Container inventory, ElectricalDataLoggerRender render) {
        super(new ElectricalDataLoggerContainer(player, inventory), player.getInventory(), Component.literal("Data Logger"));
        this.render = render;
    }

    void displayEntry() {
        config.setMessage(Component.literal(tr("Configuration")));
        config.visible = true;
        pause.visible = true;
        resetBt.visible = true;
        voltageType.visible = false;
        energyType.visible = false;
        percentType.visible = false;
        noType.visible = false;
        currentType.visible = false;
        powerType.visible = false;
        celsiusType.visible = false;
        samplingPeriod.setVisible(false);
        maxValue.setVisible(false);
        minValue.setVisible(false);
        printBt.visible = true;
        state = State.display;
    }

    void configEntry() {
        pause.visible = false;
        config.visible = true;
        config.setMessage(Component.literal(tr("Back to display")));
        resetBt.visible = false;
        printBt.visible = true;
        voltageType.visible = true;
        energyType.visible = true;
        percentType.visible = true;
        noType.visible = true;
        currentType.visible = true;
        powerType.visible = true;
        celsiusType.visible = true;
        samplingPeriod.setVisible(true);
        maxValue.setVisible(true);
        minValue.setVisible(true);
        state = State.config;
    }

    @Override
    public void initGui() {
        super.initGui();

        config = newGuiButton(176 / 2 - 50, 8 - 2, 100, "");

        //@devs: Do not translate the following elements. Please.
        voltageType = newGuiButton(176 / 2 - 75 - 2, 8 + 20 + 2 - 2, 75, tr("Voltage [V]"));
        currentType = newGuiButton(176 / 2 + 2, 8 + 20 + 2 - 2, 75, tr("Current [A]"));
        powerType = newGuiButton(176 / 2 - 75 - 2, 8 + 40 + 4 - 2, 75, tr("Power [W]"));
        celsiusType = newGuiButton(176 / 2 + 2, 8 + 40 + 4 - 2, 75, tr("Temp. [*C]"));
        percentType = newGuiButton(176 / 2 - 75 - 2, 8 + 60 + 6 - 2, 75, tr("Percent [-]%"));
        energyType = newGuiButton(176 / 2 + 2, 8 + 60 + 6 - 2, 75, tr("Energy [J]"));
        noType = newGuiButton(176 / 2 - 75 / 2 - 2, 8 + 80 + 8 - 2, 75, tr("Unit"));

        resetBt = newGuiButton(176 / 2 - 50, 8 + 20 + 2 - 2, 48, tr("Reset"));
        pause = newGuiButton(176 / 2 + 2, 8 + 20 + 2 - 2, 48, "");

        printBt = newGuiButton(176 / 2 - 48 / 2, 146, 48, tr("Print"));

        samplingPeriod = newGuiTextField(30, 124, 50);
        samplingPeriod.setText(String.valueOf(render.log.samplingPeriod));
        samplingPeriod.setComment(new String[]{tr("Sampling period")});

        maxValue = newGuiTextField(176 - 50 - 30, 124 - 7, 50);
        maxValue.setText(String.valueOf(render.log.maxValue));
        maxValue.setComment(new String[]{tr("Y-axis max")});

        minValue = newGuiTextField(176 - 50 - 30, 124 + 8, 50);
        minValue.setText(String.valueOf(render.log.minValue));
        minValue.setComment(new String[]{tr("Y-axis min")});

        displayEntry();
    }

    @Override
    public void guiObjectEvent(IGuiObject object) {
        super.guiObjectEvent(object);
        try {
            if (object == resetBt) {
                render.clientSend(ElectricalDataLoggerElement.resetId);
            } else if (object == pause) {
                render.clientSend(ElectricalDataLoggerElement.tooglePauseId);
            } else if (object == printBt) {
                render.clientSend(ElectricalDataLoggerElement.printId);
            } else if (object == currentType) {
                render.clientSetByte(ElectricalDataLoggerElement.setUnitId, DataLogs.currentType);
            } else if (object == voltageType) {
                render.clientSetByte(ElectricalDataLoggerElement.setUnitId, DataLogs.voltageType);
            } else if (object == energyType) {
                render.clientSetByte(ElectricalDataLoggerElement.setUnitId, DataLogs.energyType);
            } else if (object == percentType) {
                render.clientSetByte(ElectricalDataLoggerElement.setUnitId, DataLogs.percentType);
            } else if(object == noType) {
                render.clientSetByte(ElectricalDataLoggerElement.setUnitId, DataLogs.noType);
            } else if (object == powerType) {
                render.clientSetByte(ElectricalDataLoggerElement.setUnitId, DataLogs.powerType);
            } else if (object == celsiusType) {
                render.clientSetByte(ElectricalDataLoggerElement.setUnitId, DataLogs.celsiusType);
            } else if (object == config) {
                switch (state) {
                    case config:
                        displayEntry();
                        break;
                    case display:
                        configEntry();
                        break;
                    default:
                        break;
                }
            } else if (object == maxValue) {
                render.clientSetFloat(ElectricalDataLoggerElement.setMaxValue, NumberFormat.getInstance().parse(maxValue.getText()).floatValue());
            } else if (object == minValue) {
                render.clientSetFloat(ElectricalDataLoggerElement.setMinValue, NumberFormat.getInstance().parse(minValue.getText()).floatValue());
            } else if (object == samplingPeriod) {
                float value = NumberFormat.getInstance().parse(samplingPeriod.getText()).floatValue();
                if (value < 0.05f) value = 0.05f;
                samplingPeriod.setText(String.valueOf(value));

                render.clientSetFloat(ElectricalDataLoggerElement.setSamplingPeriodeId, value);
            }
        } catch (ParseException e) {
        }
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
        powerType.active = true;
        currentType.active = true;
        voltageType.active = true;
        celsiusType.active = true;
        percentType.active = true;
        energyType.active = true;

        switch (render.log.unitType) {
            case DataLogs.currentType:
                currentType.active = false;
                break;
            case DataLogs.voltageType:
                voltageType.active = false;
                break;
            case DataLogs.powerType:
                powerType.active = false;
                break;
            case DataLogs.celsiusType:
                celsiusType.active = false;
                break;
            case DataLogs.percentType:
                percentType.active = false;
                break;
            case DataLogs.energyType:
                energyType.active = false;
                break;
            case DataLogs.noType:
                noType.active = false;
                break;
        }

        if (render.pause)
            pause.setMessage(Component.literal(FC.DARK_YELLOW + "Paused"));
        else
            pause.setMessage(Component.literal(FC.BRIGHT_GREEN + "Running"));

        boolean a = menu.getSlot(ElectricalDataLoggerContainer.paperSlotId).getItem() != null;
        boolean b = menu.getSlot(ElectricalDataLoggerContainer.printSlotId).getItem() == null;
        printBt.active = a && b;
    }

    @Override
    public void postDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.postDraw(guiGraphics, f, x, y);
        final float bckrndMargin = 0.05f;

        if (state == State.display) {

            GL11.glPushMatrix();
            GL11.glTranslatef(leftPos + 8, topPos + 53, 0);
            GL11.glScalef(50, 50, 1f);

            GL11.glColor4f(0.15f, 0.15f, 0.15f, 1.0f);
            UtilsClient.disableTexture();
            UtilsClient.disableCulling();
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-bckrndMargin, -bckrndMargin);
            GL11.glVertex2f(3.2f + bckrndMargin, -bckrndMargin);
            GL11.glVertex2f(3.2f + bckrndMargin, 1.6f + 3 * bckrndMargin);
            GL11.glVertex2f(-bckrndMargin, 1.6f + 3 * bckrndMargin);
            GL11.glEnd();
            UtilsClient.enableCulling();
            UtilsClient.enableTexture();

            GL11.glColor4f(render.descriptor.cr, render.descriptor.cg, render.descriptor.cb, 1);
            render.log.draw(guiGraphics, 2.9f, 1.6f, render.descriptor.textColor);
            GL11.glPopMatrix();
        }
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new GuiHelperContainer(this, 176, 253, 8, 171);
    }
}

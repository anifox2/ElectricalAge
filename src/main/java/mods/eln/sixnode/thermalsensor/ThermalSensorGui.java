package mods.eln.sixnode.thermalsensor;

import mods.eln.gui.*;
import mods.eln.sim.PhysicalConstant;
import mods.eln.sixnode.electricalsensor.ElectricalSensorElement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

import java.text.NumberFormat;
import java.text.ParseException;

import static mods.eln.i18n.I18N.tr;

public class ThermalSensorGui extends GuiContainerEln<ThermalSensorContainer> {

    GuiButtonEln validate, temperatureType, powerType;
    GuiTextFieldEln lowValue, highValue;
    ThermalSensorRender render;

    public ThermalSensorGui(Player player, Container inventory, ThermalSensorRender render) {
        super(new ThermalSensorContainer(player, inventory, render.descriptor.temperatureOnly), player.getInventory(), Component.literal("Thermal Sensor"));
        this.render = render;
    }

    @Override
    public void initGui() {
        super.initGui();

        if (!render.descriptor.temperatureOnly) {
            powerType = newGuiButton(8, 8, 70, tr("Power"), (b) -> {
                render.clientSetByte(ThermalSensorElement.setTypeOfSensorId, ThermalSensorElement.powerType);
            });
            temperatureType = newGuiButton(176 - 8 - 70, 8, 70, tr("Temperature"), (b) -> {
                render.clientSetByte(ThermalSensorElement.setTypeOfSensorId, ThermalSensorElement.temperatureType);
            });

            int x = -15, y = 13;
            validate = newGuiButton(x + 8 + 50 + 4 + 50 + 4 - 26, y + (166 - 84) / 2 - 8, 50, tr("Validate"), (b) -> {
                validateValues();
            });

            lowValue = newGuiTextField(x + 8 + 50 + 4 - 26, y + (166 - 84) / 2 + 3, 50);
            lowValue.setText(String.valueOf(render.lowValue));
            lowValue.setComment(tr("Measured value\ncorresponding\nto 0% output").split("/n"));

            highValue = newGuiTextField(x + 8 + 50 + 4 - 26, y + (166 - 84) / 2 - 12, 50);
            highValue.setText(String.valueOf(render.highValue));
            highValue.setComment(tr("Measured value\ncorresponding\nto 100% output").split("\n"));
        } else {
            int x = 0, y = 0;
            validate = newGuiButton(x + 8 + 50 + 4 + 50 + 4 - 26, y + (166 - 84) / 2 - 8, 50, tr("Validate"), (b) -> {
                validateValues();
            });

            lowValue = newGuiTextField(x + 8 + 50 + 4 - 26, y + (166 - 84) / 2 + 3, 50);
            lowValue.setText(String.valueOf(render.lowValue));
            lowValue.setComment(tr("Measured temperature\ncorresponding\nto 0% output").split("/n"));

            highValue = newGuiTextField(x + 8 + 50 + 4 - 26, y + (166 - 84) / 2 - 12, 50);
            highValue.setText(String.valueOf(render.highValue));
            highValue.setComment(tr("Measured temperature\ncorresponding\nto 100% output").split("/n"));
        }
    }

    void validateValues() {
        float lowVoltage, highVoltage;
        try {
            lowVoltage = NumberFormat.getInstance().parse(lowValue.getText()).floatValue();
            highVoltage = NumberFormat.getInstance().parse(highValue.getText()).floatValue();
            render.clientSetFloat(ElectricalSensorElement.setValueId, lowVoltage - (float) PhysicalConstant.ambientTemperatureCelsius, highVoltage - (float) PhysicalConstant.ambientTemperatureCelsius);
        } catch (ParseException e) {
        }
    }

    @Override
    public void preDraw(GuiGraphics guiGraphics, float f, int x, int y) {
        super.preDraw(guiGraphics, f, x, y);
        if (!render.descriptor.temperatureOnly) {
            if (render.typeOfSensor == ThermalSensorElement.temperatureType) {
                powerType.setEnabled(true);
                temperatureType.setEnabled(false);
            } else if (render.typeOfSensor == ThermalSensorElement.powerType) {
                powerType.setEnabled(false);
                temperatureType.setEnabled(true);
            }
        }
    }

    @Override
    public GuiHelperContainer newHelper() {
        return new HelperStdContainer(this);
    }
}

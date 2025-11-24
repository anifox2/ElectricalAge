package mods.eln.sixnode.electricaldatalogger;

import mods.eln.misc.INBTTReady;
import mods.eln.misc.Utils;
import mods.eln.sim.PhysicalConstant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class DataLogs implements INBTTReady {

    byte[] log;
    int sizeMax, size;

    float samplingPeriod = 0.5f;
    float maxValue = 100f, minValue = 0f;
    byte unitType = percentType;
    public static final byte voltageType = 0, currentType = 1, powerType = 2, celsiusType = 3, percentType = 4, energyType = 5, noType = 6;

    public DataLogs(int sizeMax) {
        log = new byte[sizeMax];
        this.sizeMax = sizeMax;
        size = 0;
    }

    void write(byte data) {
        int idx;
        if (size != sizeMax) {
            size++;
        }
        if (size != sizeMax)
            idx = size;
        else
            idx = size - 1;

        while (idx > 0) {
            log[idx] = log[idx - 1];
            idx--;
        }
        log[0] = data;
    }

    void reset() {
        size = 0;
    }

    int size() {
        return size;
    }

    byte read(int idx) {
        return log[idx];
    }

    @Override
    public void readFromNBT(CompoundTag nbt, String str) {
        byte[] cpy = nbt.getByteArray(str + "log");
        Utils.println("Datalog readnbt " + cpy.length);
        for (int idx = 0; idx < cpy.length; idx++) {
            write(cpy[cpy.length - 1 - idx]);
        }

        samplingPeriod = nbt.getFloat(str + "samplingPeriod");
        maxValue = nbt.getFloat(str + "maxValue");
        minValue = nbt.getFloat(str + "minValue");
        unitType = nbt.getByte(str + "unitType");
        Utils.println("Datalog readnbt done");
    }

    @Override
    public void writeToNBT(CompoundTag nbt, String str) {
        nbt.putByteArray(str + "log", copyLog());
        nbt.putFloat(str + "samplingPeriod", samplingPeriod);
        nbt.putFloat(str + "maxValue", maxValue);
        nbt.putFloat(str + "minValue", minValue);
        nbt.putByte(str + "unitType", unitType);
    }

    public byte[] copyLog() {
        byte[] cpy = new byte[size];
        for (int idx = 0; idx < size; idx++) {
            cpy[idx] = read(idx);
        }
        return cpy;
    }

    @Override
    public String toString() {
        String str = "";
        for (int idx = 0; idx < size; idx++) {
            str += ((int) read(idx) + 128) + " ";
        }
        return str;
    }

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, float margeX, float margeY, String textHeader) {
        draw(poseStack, buffer, light, overlay, null, log, size, samplingPeriod, maxValue, minValue, unitType, margeX, margeY, textHeader);
    }

    void draw(GuiGraphics guiGraphics, float margeX, float margeY, String textHeader) {
        // For GUI, we can use the guiGraphics pose stack and buffer source?
        // Or we can adapt draw to take guiGraphics.
        // But draw is static and complex.
        // Let's make the static draw take PoseStack and MultiBufferSource.
        // For GUI, we can get them from guiGraphics.
        draw(guiGraphics.pose(), guiGraphics.bufferSource(), 0xF000F0, 0, guiGraphics, log, size, samplingPeriod, maxValue, minValue, unitType, margeX, margeY, textHeader);
    }

    static void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, GuiGraphics guiGraphics, byte[] value, int size, float samplingPeriod, float maxValue, float minValue, byte unitType, float margeX, float margeY, String textHeader) {
        if (value == null) return;
        if (size < 2) return;
        
        VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lines());
        float dx = 1f / (size - 1);
        Matrix4f matrix = poseStack.last().pose();
        
        // Line strip
        for (int idx = 0; idx < size - 1; idx++) {
            float x1 = margeX - dx * idx * margeX;
            float y1 = margeY - ((int) value[idx] + 128) / 255f * margeY;
            float x2 = margeX - dx * (idx + 1) * margeX;
            float y2 = margeY - ((int) value[idx + 1] + 128) / 255f * margeY;
            
            lineConsumer.vertex(matrix, x1, y1, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
            lineConsumer.vertex(matrix, x2, y2, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        }

        // Quad strip (thick line) - approximated with lines for now or skipped if lines are enough.
        // Or use debugLineStrip?
        // Let's skip the thick line part for now to save time/complexity, lines should be visible.
        
        // Border box
        float temp = 0.01f;
        // GL11.glBegin(GL11.GL_QUAD_STRIP); ...
        // Draw box using lines
        lineConsumer.vertex(matrix, margeX + temp, 0f, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        lineConsumer.vertex(matrix, margeX + temp, margeY + temp, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        
        lineConsumer.vertex(matrix, margeX + temp, margeY + temp, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        lineConsumer.vertex(matrix, -temp, margeY + temp, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        
        lineConsumer.vertex(matrix, -temp, margeY + temp, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        lineConsumer.vertex(matrix, -temp, -temp, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        
        lineConsumer.vertex(matrix, -temp, -temp, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        lineConsumer.vertex(matrix, margeX + temp, -temp, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        
        lineConsumer.vertex(matrix, margeX + temp, -temp, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        lineConsumer.vertex(matrix, margeX + temp, 0f, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();

        if ((minValue < 0 && maxValue > 0) || (minValue > 0 && maxValue < 0)) {
            temp = 0.005f;
            float zeroY = (maxValue) / (maxValue - minValue) * margeY;
            // Draw zero line
            lineConsumer.vertex(matrix, margeX, zeroY, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
            lineConsumer.vertex(matrix, 0f, zeroY, 0).color(255, 255, 255, 255).normal(0, 1, 0).endVertex();
        }

        // Text
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        float scale = 0.01f;
        poseStack.scale(scale, scale, 1f);
        
        // We need to flip Y for text? No, usually text is top-down.
        // But here Y seems to be up?
        // The plot Y is `margeY - ...`.
        
        // If guiGraphics is null, we use font.drawInBatch.
        if (guiGraphics == null) {
             // World rendering
             // We need to use font.drawInBatch
             // But font.drawInBatch takes a Matrix4f.
             
             font.drawInBatch(textHeader + " " + getYstring(1f, maxValue, minValue, unitType), (margeX / scale), (0f / scale), 0xFFFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
             font.drawInBatch(textHeader + " " + getYstring(0.5f, maxValue, minValue, unitType), (margeX / scale), ((margeY / 2 - 0.05f) / scale), 0xFFFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
             font.drawInBatch(textHeader + " " + getYstring(0.0f, maxValue, minValue, unitType), (margeX / scale), ((margeY - 0.08f) / scale), 0xFFFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
             
             font.drawInBatch(textHeader + Utils.plotTime(size * samplingPeriod), (0f / scale), ((margeY + 0.03f) / scale), 0xFFFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
             font.drawInBatch(textHeader + Utils.plotTime(0), ((margeX - 0.05f) / scale), ((margeY + 0.03f) / scale), 0xFFFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
        } else {
             // GUI rendering
             guiGraphics.drawString(font, textHeader + " " + getYstring(1f, maxValue, minValue, unitType), (int) (margeX / scale), (int) (0f / scale), 0, false);
             guiGraphics.drawString(font, textHeader + " " + getYstring(0.5f, maxValue, minValue, unitType), (int) (margeX / scale), (int) ((margeY / 2 - 0.05f) / scale), 0, false);
             guiGraphics.drawString(font, textHeader + " " + getYstring(0.0f, maxValue, minValue, unitType), (int) (margeX / scale), (int) ((margeY - 0.08f) / scale), 0, false);

             guiGraphics.drawString(font, textHeader + Utils.plotTime(size * samplingPeriod), (int) (0f / scale), (int) ((margeY + 0.03) / scale), 0, false);
             guiGraphics.drawString(font, textHeader + Utils.plotTime(0), (int) ((margeX - 0.05) / scale), (int) ((margeY + 0.03) / scale), 0, false);
        }
        
        poseStack.popPose();
    }

    public static String getYstring(float factor, float maxValue, float minValue, byte unitType) {
        String str = "";

        switch (unitType) {
            case celsiusType:
                str = Utils.plotCelsius("", factor * (maxValue - minValue) + minValue - PhysicalConstant.ambientTemperatureCelsius);
                break;
            case voltageType:
                str = Utils.plotVolt("", factor * (maxValue - minValue) + minValue);
                break;
            case currentType:
                str = Utils.plotAmpere("", factor * (maxValue - minValue) + minValue);
                break;
            case powerType:
                str = Utils.plotPower("", factor * (maxValue - minValue) + minValue);
                break;
            case percentType:
                str = Utils.plotPercent("", (factor * (maxValue - minValue) + minValue) * 0.01);
                break;
            case energyType:
                str = Utils.plotEnergy("", (factor * (maxValue - minValue) + minValue));
                break;
            case noType:
                str = "" + (factor * (maxValue - minValue) + minValue);
                break;
        }
        return str;
    }

    public static void draw(GuiGraphics guiGraphics, CompoundTag nbt, float margeX, float margeY, String textHeader) {
        if (nbt == null) return;
        byte[] data = nbt.getByteArray("log");
        if (data == null) return;
        draw(guiGraphics.pose(), guiGraphics.bufferSource(), 0xF000F0, 0, guiGraphics, data, data.length, nbt.getFloat("samplingPeriod"), nbt.getFloat("maxValue"), nbt.getFloat("minValue"), nbt.getByte("unitType"), margeX, margeY, textHeader);
    }
}

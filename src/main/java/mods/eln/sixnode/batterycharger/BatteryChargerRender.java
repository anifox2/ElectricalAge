package mods.eln.sixnode.batterycharger;

import mods.eln.cable.CableRenderDescriptor;
import mods.eln.misc.Coordinate;
import net.minecraft.core.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.Utils;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.DataInputStream;
import java.io.IOException;

public class BatteryChargerRender extends SixNodeElementRender {

    BatteryChargerDescriptor descriptor;

    Coordinate coord;
    boolean[] charged = new boolean[]{false, false, false, false};
    boolean[] batteryPresence = new boolean[]{false, false, false, false};

    float alpha = 0;

    ItemStack[] stacks = new ItemStack[4];
    boolean powerOn;
    private float voltage;

    public BatteryChargerRender(SixNodeEntity tileEntity, Direction side, SixNodeDescriptor descriptor) {
        super(tileEntity, mods.eln.misc.Direction.fromMCDirection(side), descriptor);
        this.descriptor = (BatteryChargerDescriptor) descriptor;

        coord = new Coordinate(tileEntity);
    }

    @Override
    public void draw() {
        super.draw();

        drawPowerPin(descriptor.pinDistance);

        if (side.isY()) {
            front.right().glRotateOnX();
        }

        drawItemStack(stacks[0], 0.1875, 0.15625, 0.15625, alpha, 0.2f);
        drawItemStack(stacks[1], 0.1875, 0.15625, -0.15625, alpha, 0.2f);
        drawItemStack(stacks[2], 0.1875, -0.15625, 0.15625, alpha, 0.2f);
        drawItemStack(stacks[3], 0.1875, -0.15625, -0.15625, alpha, 0.2f);

        descriptor.draw(batteryPresence, charged);
    }

    @Override
    public void refresh(float deltaT) {
        alpha += 90 * deltaT;
        if (alpha > 360) alpha -= 360;
    }

    public void drawItemStack(ItemStack stack, double x, double y, double z, float roty, float scale) {
        if (stack == null || stack.isEmpty()) return;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glRotatef(90, 0f, 1f, 0f);
        GL11.glRotatef(roty, 0, 1, 0);
        GL11.glScalef(scale, scale, scale);
        GL11.glTranslatef(0.0f, -0.25f, 0.0f);
        
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, new PoseStack(), Minecraft.getInstance().renderBuffers().bufferSource(), blockEntity.getLevel(), 0);
        
        GL11.glPopMatrix();
    }

    @Nullable
    @Override
    public CableRenderDescriptor getCableRender(@NotNull LRDU lrdu) {
        return descriptor.cable.render;
    }

    @Nullable
    @Override
    public Screen newGuiDraw(@NotNull mods.eln.misc.Direction side, @NotNull Player player) {
        return new BatteryChargerGui(this, player, inventory);
    }

    @Override
    public void publishUnserialize(DataInputStream stream) {
        super.publishUnserialize(stream);
        try {
            powerOn = stream.readBoolean();
            voltage = stream.readFloat();

            for (int idx = 0; idx < 4; idx++) {
                stacks[idx] = Utils.unserializeItemStack(stream);
            }

            byte temp = stream.readByte();
            for (int idx = 0; idx < 4; idx++) {
                charged[idx] = (temp & 1) != 0;
                temp = (byte) (temp >> 1);
            }
            temp = stream.readByte();
            for (int idx = 0; idx < 4; idx++) {
                batteryPresence[idx] = (temp & 1) != 0;
                temp = (byte) (temp >> 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SixNodeElementInventory inventory = new SixNodeElementInventory(5, 64, this);
}

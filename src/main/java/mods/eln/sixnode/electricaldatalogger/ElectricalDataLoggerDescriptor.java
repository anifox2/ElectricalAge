package mods.eln.sixnode.electricaldatalogger;

import mods.eln.Eln;
import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class ElectricalDataLoggerDescriptor extends SixNodeDescriptor {

    Obj3D obj;
    Obj3DPart main, led, reflection;
    float sx, sy, sz;
    float tx, ty, tz;
    float rx, ry, rz, ra;
    float mx, my;

    float cr, cg, cb;

    float reflc;

    public boolean onFloor;
    public String textColor;

    public ElectricalDataLoggerDescriptor(String name, boolean onFloor, String objName, float cr, float cg, float cb, String textColor) {
        super(name, ElectricalDataLoggerElement.class, ElectricalDataLoggerRender.class);
        this.cb = cb;
        this.cr = cr;
        this.cg = cg;
        this.onFloor = onFloor;
        this.textColor = textColor;
        obj = Eln.obj.getObj(objName);
        if (obj != null) {
            main = obj.getPart("main");
            reflection = obj.getPart("reflection");
            if (main != null) {
                sx = main.getFloat("sx");
                sy = main.getFloat("sy");
                sz = main.getFloat("sz");
                tx = main.getFloat("tx");
                ty = main.getFloat("ty");
                tz = main.getFloat("tz");
                rx = main.getFloat("rx");
                ry = main.getFloat("ry");
                rz = main.getFloat("rz");
                ra = main.getFloat("ra");
                mx = main.getFloat("mx");
                my = main.getFloat("my");
                reflc = main.getFloat("reflc");
                led = obj.getPart("led");
            }
        }

        if (onFloor) {
            setPlaceDirection(Direction.YN);
        }

        voltageLevelColor = VoltageLevelColor.SignalVoltage;
    }

    void draw(DataLogs log, Direction side, LRDU front, int objPosMX, int objPosMZ, byte color) {
        if (onFloor || side.isY()) front.glRotateOnX();
        if (!onFloor && side.isNotY()) GL11.glRotatef(90, 1, 0, 0);
        //GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (main != null) {
            Utils.setGlColorFromDye(color);
            main.draw();
            GL11.glColor3f(1f, 1f, 1f);
        }
        //GL11.glEnable(GL11.GL_TEXTURE_2D);

        //Glass (reflections)
        mods.eln.misc.UtilsClient.INSTANCE.enableBlend();
        obj.bindTexture("Reflection.png");
        float rotYaw = Minecraft.getInstance().player.getYRot() / 360.f;
        float rotPitch = Minecraft.getInstance().player.getXRot() / 180.f;
        float pos = (((float) Minecraft.getInstance().player.getX()) - ((float) (objPosMX * 2)) + ((float) Minecraft.getInstance().player.getZ()) - ((float) (objPosMZ * 2))) / 24.f;
        GL11.glColor4f(1, 1, 1, reflc);
        reflection.draw(rotYaw + pos, rotPitch * 0.857f);
        mods.eln.misc.UtilsClient.INSTANCE.disableBlend();

        //Plot
        if (log != null) {
            mods.eln.misc.UtilsClient.INSTANCE.disableLight();
            // GL11.glPushMatrix();
            mods.eln.misc.UtilsClient.ledOnOffColor(true);
            if (led != null) led.draw();

            mods.eln.misc.UtilsClient.glDefaultColor();

            GL11.glTranslatef(tx, ty, tz);
            GL11.glRotatef(ra, rx, ry, rz);
            GL11.glScalef(sx, sy, sz);
            GL11.glColor4f(cr, cg, cb, 1);
            log.draw(mx, my, textColor);

            mods.eln.misc.UtilsClient.glDefaultColor();

            mods.eln.misc.UtilsClient.INSTANCE.enableLight();
        }
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addSignal(newItemStack());
    }

    @Override
    public boolean hasVolume() {
        return onFloor;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String[] lines = tr("Measures the voltage of an\nelectrical signal and plots\nthe data in real time.").split("\n");
        for (String line : lines) {
            tooltip.add(net.minecraft.network.chat.Component.literal(line));
        }
        tooltip.add(net.minecraft.network.chat.Component.literal(tr("It can store up to 256 points.")));
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull Direction side, @NotNull Player player) {
        LRDU front = super.getFrontFromPlace(side, player);
        if (onFloor) {
            return front.inverse();
        } else {
            return front;
        }
    }
}

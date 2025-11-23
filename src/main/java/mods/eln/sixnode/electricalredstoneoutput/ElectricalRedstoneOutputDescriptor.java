package mods.eln.sixnode.electricalredstoneoutput;

import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class ElectricalRedstoneOutputDescriptor extends SixNodeDescriptor {

    public float[] pinDistance;

    Obj3D obj;
    Obj3DPart main, led;

    public ElectricalRedstoneOutputDescriptor(String name, Obj3D obj) {
        super(name, ElectricalRedstoneOutputElement.class, ElectricalRedstoneOutputRender.class);
        this.obj = obj;
        if (obj != null) {
            main = obj.getPart("main");
            led = obj.getPart("led");

            pinDistance = Utils.getSixNodePinDistance(main);
        }

        voltageLevelColor = VoltageLevelColor.SignalVoltage;
    }

    void draw(int redstone) {
        //LRDU.Down.glRotateOnX();
        if (main != null) main.draw();

        float light = redstone / 15f;
        GL11.glColor4f(light, light, light, 1f);
        UtilsClient.drawLight(led);
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addSignal(newItemStack());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        for (String s : tr("Converts electrical voltage\ninto a Redstone signal.").split("\n")) {
            tooltip.add(Component.literal(s));
        }
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull mods.eln.misc.Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).right();
    }
}

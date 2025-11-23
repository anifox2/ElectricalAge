package mods.eln.sixnode.electricallightsensor;

import mods.eln.Eln;
import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class ElectricalLightSensorDescriptor extends SixNodeDescriptor {

    private Obj3DPart main;
    public boolean dayLightOnly;
    public float[] pinDistance;

    Obj3D obj;

    public ElectricalLightSensorDescriptor(String name, Obj3D obj, boolean dayLightOnly) {
        super(name, ElectricalLightSensorElement.class, ElectricalLightSensorRender.class);
        this.obj = obj;
        this.dayLightOnly = dayLightOnly;

        if (obj != null) {
            main = obj.getPart("main");
            pinDistance = Utils.getSixNodePinDistance(main);
        }

        voltageLevelColor = VoltageLevelColor.SignalVoltage;
    }

    void draw() {
        if (main != null) main.draw();
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addSignal(newItemStack());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (dayLightOnly) {
            String[] lines = tr("Provides an electrical voltage\nwhich is proportional to\nthe intensity of daylight.").split("\n");
            for (String line : lines) tooltip.add(net.minecraft.network.chat.Component.literal(line));
            tooltip.add(net.minecraft.network.chat.Component.literal(tr("0V at night, %1$sV at noon.", Utils.plotValue(Eln.SVU))));
        } else {
            String[] lines = tr("Provides an electrical voltage\nin the presence of light.").split("\n");
            for (String line : lines) tooltip.add(net.minecraft.network.chat.Component.literal(line));
        }
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).right();
    }
}

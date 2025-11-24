package mods.eln.sixnode.electricalweathersensor;

import mods.eln.Eln;
import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class ElectricalWeatherSensorDescriptor extends SixNodeDescriptor {

    private Obj3DPart main;
    public float[] pinDistance;

    Obj3D obj;

    public ElectricalWeatherSensorDescriptor(String name, Obj3D obj) {
        super(name, ElectricalWeatherSensorElement.class, ElectricalWeatherSensorRender.class);
        this.obj = obj;

        if (obj != null) {
            main = obj.getPart("main");

            pinDistance = Utils.getSixNodePinDistance(main);
        }

        voltageLevelColor = VoltageLevelColor.SignalVoltage;
    }

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (main != null) main.draw(poseStack, buffer, light, overlay);
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addSignal(newItemStack());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String[] lines = tr("Provides an electrical signal\ndepending the actual weather.").split("\n");
        for (String line : lines) {
            tooltip.add(Component.literal(line));
        }
        tooltip.add(Component.literal(String.format(tr("Clear: %1$sV"), 0)));
        tooltip.add(Component.literal(String.format(tr("Rain: %1$sV"), Utils.plotValue(Eln.SVU / 2))));
        tooltip.add(Component.literal(String.format(tr("Storm: %1$sV"), Utils.plotValue(Eln.SVU))));
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).right();
    }
}

package mods.eln.sixnode.electricalredstoneinput;

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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class ElectricalRedstoneInputDescriptor extends SixNodeDescriptor {

    public float[] pinDistance;

    Obj3D obj;
    Obj3DPart main, led;

    public ElectricalRedstoneInputDescriptor(String name, Obj3D obj) {
        super(name, ElectricalRedstoneInputElement.class, ElectricalRedstoneInputRender.class);
        //obj = Eln.instance.obj.getObj(objName);
        this.obj = obj;
        if (obj != null) {
            main = obj.getPart("main");
            led = obj.getPart("led");

            pinDistance = Utils.getSixNodePinDistance(main);
        }

        voltageLevelColor = VoltageLevelColor.SignalVoltage;
    }

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, int redstone) {
        //LRDU.Down.glRotateOnX();
        if (main != null) main.draw(poseStack, buffer, light, overlay);

        float lightVal = redstone / 15f;
        // GL11.glColor4f(light, light, light, 1f);
        UtilsClient.drawLight(led, poseStack, buffer, light, overlay, lightVal, lightVal, lightVal, 1f);
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addSignal(newItemStack());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String[] lines = tr("Converts Redstone signal\nto an electrical voltage.").split("\n");
        for (String line : lines) {
            tooltip.add(Component.literal(line));
        }
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).right();
    }
}

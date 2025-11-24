package mods.eln.sixnode.electricalgatesource;

import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.RealisticEnum;
import mods.eln.misc.VoltageLevelColor;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class ElectricalGateSourceDescriptor extends SixNodeDescriptor {

    // onOffOnly: if true, is a button. Otherwise, it's a signal trimmer.
    public boolean onOffOnly;

    // autoReset: if true, this button will press and release. Otherwise, it will act like a latch
    public boolean autoReset = false;


    enum ObjType {Pot, Button}

    // objType is often null... IDEA flags leverTx as unused as well.
    ObjType objType;
    float leverTx;
    ElectricalGateSourceRenderObj render;

    public ElectricalGateSourceDescriptor(String name, ElectricalGateSourceRenderObj render, boolean onOffOnly,
                                          String iconName) {
        super(name, ElectricalGateSourceElement.class, ElectricalGateSourceRender.class, iconName);
        this.render = render;
        this.onOffOnly = onOffOnly;

        voltageLevelColor = VoltageLevelColor.SignalVoltage;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        // super.appendHoverText(stack, level, tooltip, flag);
        if (!onOffOnly) {
            String[] lines = tr("Provides configurable signal\nvoltage.").split("\n");
            for (String line : lines) tooltip.add(net.minecraft.network.chat.Component.literal(line));
        } else {
            if (autoReset) {
                String[] lines = tr("Acts like a\npush button.").split("\n");
                for (String line : lines) tooltip.add(net.minecraft.network.chat.Component.literal(line));
            } else {
                String[] lines = tr("Acts like a\ntoggle switch.").split("\n");
                for (String line : lines) tooltip.add(net.minecraft.network.chat.Component.literal(line));
            }
        }
    }

    public void setWithAutoReset() {
        autoReset = true;
    }

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, float factor, float distance, BlockEntity e) {
        render.draw(poseStack, buffer, light, overlay, factor, distance, e);
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addSignal(newItemStack());
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).inverse();
    }
}

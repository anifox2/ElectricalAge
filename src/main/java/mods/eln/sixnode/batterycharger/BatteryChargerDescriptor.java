package mods.eln.sixnode.batterycharger;

import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.sim.mna.component.Resistor;
import mods.eln.sim.nbt.NbtElectricalLoad;
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

import net.minecraft.resources.ResourceLocation;

public class BatteryChargerDescriptor extends SixNodeDescriptor {

    Obj3D obj;
    Obj3DPart main;
    Obj3DPart[] leds = new Obj3DPart[4];
    ElectricalCableDescriptor cable;
    VoltageLevelColor voltageLevelColor;

    public double nominalVoltage;
    public double nominalPower;
    public double Rp;
    public double pinDistance = 0.0;

    public BatteryChargerDescriptor(String name, Obj3D obj, ElectricalCableDescriptor cable, double nominalVoltage, double nominalPower) {
        super(name, BatteryChargerElement.class, BatteryChargerRender.class);
        this.obj = obj;
        this.cable = cable;
        this.nominalVoltage = nominalVoltage;
        this.nominalPower = nominalPower;
        this.Rp = nominalVoltage * nominalVoltage / nominalPower;

        if (obj != null) {
            main = obj.getPart("main");
            for (int idx = 0; idx < 4; idx++) {
                leds[idx] = obj.getPart("led" + idx);
            }
        }
        voltageLevelColor = VoltageLevelColor.fromVoltage(nominalVoltage);
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addEnergy(newItemStack());
        Data.addUtilities(newItemStack());
    }

    public void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, boolean[] presence, boolean[] charged) {
        if (main != null)
            main.draw(poseStack, buffer, light, overlay);

        int idx = 0;
        for (Obj3DPart led : leds) {
            if (presence != null && presence[idx]) {
                java.awt.Color color = UtilsClient.ledOnOffColorC(charged[idx]);
                UtilsClient.drawLight(led, poseStack, buffer, light, overlay, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 1f);
            } else {
                com.mojang.blaze3d.vertex.VertexConsumer consumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.solid());
                led.drawColored(poseStack, consumer, light, overlay, (int)(0.2f * 255), (int)(0.2f * 255), (int)(0.2f * 255), 255);
            }
            idx++;
        }
    }

    public void applyTo(NbtElectricalLoad powerLoad) {
        cable.applyTo(powerLoad);
    }

    public void setRp(Resistor powerload, boolean powerOn) {
        if (!powerOn)
            powerload.highImpedance();
        else
            powerload.setResistance(Rp);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        for (String s : tr("Can be used to recharge\nelectrical items like:\nFlash Light, X-Ray scanner\nand Portable Battery ...").split("\n")) {
            tooltip.add(Component.literal(s));
        }
        tooltip.add(Component.literal(tr("Nominal power: %1$W", Utils.plotValue(nominalPower))));
    }

    @Override
    public RealisticEnum addRealismContext(List<String> list) {
        super.addRealismContext(list);
        list.add(tr("This battery charger doesn't take into account battery chemistry"));
        return RealisticEnum.IDEAL;
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).inverse();
    }
}

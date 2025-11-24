package mods.eln.sixnode.resistor;

import mods.eln.Eln;
import mods.eln.misc.*;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static mods.eln.i18n.I18N.tr;

import mods.eln.sim.IResistorDescriptor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Created by svein on 05/08/15.
 */
public class ResistorDescriptor extends SixNodeDescriptor implements IResistorDescriptor {

    public final boolean isRheostat;
    public double thermalCoolLimit = -100;
    public double thermalWarmLimit = Eln.cableWarmLimit;
    public double thermalMaximalPowerDissipated = 1000;
    public double thermalNominalHeatTime = 120;
    public double thermalConductivityTao = Eln.cableThermalConductionTao;
    public double tempCoef;
    Obj3D.Obj3DPart ResistorBaseExtension, ResistorCore, ResistorTrack, ResistorWiper, Base, Cables;
    IFunction series;
    private Obj3D obj;

    @Override
    public void draw(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, boolean signal) {
        if (Base != null) Base.draw(poseStack, consumer, packedLight, packedOverlay);
        if (ResistorBaseExtension != null) ResistorBaseExtension.draw(poseStack, consumer, packedLight, packedOverlay);
        if (ResistorCore != null) ResistorCore.draw(poseStack, consumer, packedLight, packedOverlay);
        if (Cables != null) Cables.draw(poseStack, consumer, packedLight, packedOverlay);

        if (isRheostat) {
            if (ResistorTrack != null) ResistorTrack.draw(poseStack, consumer, packedLight, packedOverlay);
            if (ResistorWiper != null) ResistorWiper.draw(poseStack, consumer, packedLight, packedOverlay);
        }
    }

    @Override
    public double getTempCoef() {
        return tempCoef;
    }

    @Override
    public boolean isRheostat() {
        return isRheostat;
    }


    public ResistorDescriptor(String name,
                              Obj3D obj,
                              IFunction series,
                              double tempCoef,
                              boolean isRheostat) {
        super(name, ResistorElement.class, ResistorRender.class);
        this.obj = obj;
        this.series = series;
        this.tempCoef = tempCoef;
        this.isRheostat = isRheostat;
        if (obj != null) {
            ResistorBaseExtension = obj.getPart("ResistorBaseExtention");
            ResistorCore = obj.getPart("ResistorCore");
            ResistorTrack = obj.getPart("ResistorTrack");
            ResistorWiper = obj.getPart("ResistorWiper");
            Base = obj.getPart("Base");
            Cables = obj.getPart("CapacitorCables");
        }
        voltageLevelColor = VoltageLevelColor.Neutral;
    }

    public double getRsValue(Container inventory) {
        ItemStack core = inventory.getItem(ResistorContainer.coreId);

        if (core.isEmpty()) return series.getValue(0);
        return series.getValue(core.getCount());
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addEnergy(newItemStack());
    }

    void draw(float wiperPos) {
        //UtilsClient.disableCulling();
        //UtilsClient.disableTexture();
        //GL11.glRotatef(90, 1, 0, 0);


        if (null != Base) Base.draw();
        if (null != ResistorBaseExtension) ResistorBaseExtension.draw();
        if (null != ResistorCore) ResistorCore.draw();
        if (null != Cables) Cables.draw();

        if (isRheostat) {
            final float wiperSpread = 0.238f;
            wiperPos = (wiperPos - 0.5f) * wiperSpread * 2;
            ResistorTrack.draw();
            GL11.glTranslatef(0, 0, wiperPos);
            ResistorWiper.draw();
            // GL11.glTranslatef(-wiperPos, 0, 0);
        }
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).left();
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("It's a resistor"));
    }

    @Override
    public RealisticEnum addRealismContext(List<String> list) {
        super.addRealismContext(list);
        return RealisticEnum.REALISTIC;
    }
}

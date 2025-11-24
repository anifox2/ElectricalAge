package mods.eln.sixnode.electricalalarm;

import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class ElectricalAlarmDescriptor extends SixNodeDescriptor {

    public float[] pinDistance;

    int light;
    Obj3D obj;
    Obj3DPart main, rot, lightPart;

    ResourceLocation onTexture, offTexture;
    String soundName;
    double soundTime;
    float soundLevel;
    public float rotSpeed = 0f;

    public ElectricalAlarmDescriptor(String name, Obj3D obj, int light, String soundName, double soundTime, float soundLevel) {
        super(name, ElectricalAlarmElement.class, ElectricalAlarmRender.class);
        this.obj = obj;
        this.soundName = soundName;
        this.soundTime = soundTime;
        this.soundLevel = soundLevel;
        this.light = light;

        if (obj != null) {
            main = obj.getPart("main");
            rot = obj.getPart("rot");
            lightPart = obj.getPart("light");

            onTexture = obj.getModelResourceLocation(obj.getString("onTexture"));
            offTexture = obj.getModelResourceLocation(obj.getString("offTexture"));
            if (rot != null)
                rotSpeed = rot.getFloat("speed");
            pinDistance = Utils.getSixNodePinDistance(main);
        }

        voltageLevelColor = VoltageLevelColor.SignalVoltage;
        setDefaultIcon("electricalalarm");
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addUtilities(newItemStack());
    }

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, boolean warm, float rotAlpha) {
        // Texture switching not fully supported yet with MultiBufferSource without modifying Obj3DPart
        // if (warm) UtilsClient.bindTexture(onTexture);
        // else UtilsClient.bindTexture(offTexture);
        
        if (main != null) main.draw(poseStack, buffer, light, overlay);
        
        if (rot != null) {
            // GL11.glDisable(GL11.GL_CULL_FACE);
            // GL11.glColor3f(1.0f, 1.0f, 1.0f);
            
            int rotLight = light;
            if (warm) rotLight = 15728880; // Max light
            
            rot.draw(poseStack, buffer, rotLight, overlay, rotAlpha, 1f, 0f, 0f);
            
            // GL11.glEnable(GL11.GL_CULL_FACE);
        }
        if (lightPart != null) {
            // UtilsClient.drawLightNoBind(lightPart);
            UtilsClient.drawLight(lightPart, poseStack, buffer, light, overlay, 1f, 0f, 0f, 1f);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<net.minecraft.network.chat.Component> tooltipComponents, net.minecraft.world.item.TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        String[] lines = tr("Emits an acoustic alarm if\nthe input signal is high").split("\n");
        for (String line : lines) {
            tooltipComponents.add(net.minecraft.network.chat.Component.literal(line));
        }
    }

    @Nullable
    @Override
    public LRDU getFrontFromPlace(@NotNull mods.eln.misc.Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).inverse();
    }
}

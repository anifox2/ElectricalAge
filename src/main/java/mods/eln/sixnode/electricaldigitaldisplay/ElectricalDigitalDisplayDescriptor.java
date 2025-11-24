package mods.eln.sixnode.electricaldigitaldisplay;

import mods.eln.misc.Obj3D;
import mods.eln.misc.Utils;
import mods.eln.misc.UtilsClient;
import mods.eln.misc.VoltageLevelColor;
import mods.eln.node.six.SixNodeDescriptor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class ElectricalDigitalDisplayDescriptor extends SixNodeDescriptor {
    protected Obj3D obj;
    protected Obj3D.Obj3DPart digits[] = new Obj3D.Obj3DPart[4];
    protected Obj3D.Obj3DPart dots[] = new Obj3D.Obj3DPart[5];
    protected Obj3D.Obj3DPart colons[] = new Obj3D.Obj3DPart[3];
    protected Obj3D.Obj3DPart base, glass;
    public float pinDistance[];

    public static int DOT_STATES = 256;

    enum Style {LED}

    public static final byte netSetRange = 1;

    public ElectricalDigitalDisplayDescriptor(String name, Obj3D obj_) {
        super(name, ElectricalDigitalDisplayElement.class, ElectricalDigitalDisplayRender.class);
        obj = obj_;
        base = obj.getPart("base");
        pinDistance = Utils.getSixNodePinDistance(base);
        glass = obj.getPart("glass");
        for(int i = 0; i < 4; i++) {
            digits[i] = obj.getPart("digit" + i);
        }
        for(int i = 0; i < 5; i++) {
            dots[i] = obj.getPart("dot" + i);
        }
        for(int i = 1; i < 4; i++) {
            colons[i - 1] = obj.getPart("colon" + i);
        }
        voltageLevelColor = VoltageLevelColor.Neutral;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("Displays signal value."));
    }

    private void bitToColor(int i) {
        if(i != 0) GL11.glColor3f(0.95f, 0.0f, 0.0f);
        else GL11.glColor3f(0.0f, 0.0f, 0.0f);
    }

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, int value, boolean strobe, Style style) { draw(poseStack, buffer, light, overlay, value, strobe, style, 0, 0); }
    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, int value, boolean strobe, Style style, int dye) { draw(poseStack, buffer, light, overlay, value, strobe, style, dye, 0); }

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, int value, boolean strobe, Style style, int dye, int dotconfig) {
        if (base != null) base.draw(poseStack, buffer, light, overlay);
        if (glass != null) glass.draw(poseStack, buffer, light, overlay);

        float r, g, b;
        if (dye == 0) {
            r = 1f; g = 0f; b = 0f;
        } else {
            float[] color = net.minecraft.world.item.DyeColor.byId(dye).getTextureDiffuseColors();
            r = color[0]; g = color[1]; b = color[2];
        }
        int ri = (int)(r * 255);
        int gi = (int)(g * 255);
        int bi = (int)(b * 255);
        int ai = 255;

        ResourceLocation texture = digits[0].getTextureResource();
        if (texture == null) texture = new ResourceLocation("eln", "textures/missing.png");
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(texture));

        if (strobe) {
            int digit;
            digit = value % 10;
            digits[0].drawColored(poseStack, consumer, light, overlay, digit / 16.f, 0f, ri, gi, bi, ai);
            digit = (value / 10) % 10;
            digits[1].drawColored(poseStack, consumer, light, overlay, digit / 16.f, 0f, ri, gi, bi, ai);
            digit = (value / 100) % 10;
            digits[2].drawColored(poseStack, consumer, light, overlay, digit / 16.f, 0f, ri, gi, bi, ai);
            digit = (value / 1000) % 10;
            digits[3].drawColored(poseStack, consumer, light, overlay, digit / 16.f, 0f, ri, gi, bi, ai);

            for (int i = 0; i < 5; i++) {
                if (((dotconfig >> i) & 1) != 0) {
                    dots[i].drawColored(poseStack, consumer, light, overlay, ri, gi, bi, ai);
                }
            }
            for (int i = 0; i < 3; i++) {
                if (((dotconfig >> (i + 5)) & 1) != 0) {
                    colons[i].drawColored(poseStack, consumer, light, overlay, ri, gi, bi, ai);
                }
            }
        } else {
            int dimRi = (int)(r * 0.1f * 255);
            int dimGi = (int)(g * 0.1f * 255);
            int dimBi = (int)(b * 0.1f * 255);
            for (int i = 0; i < 4; i++) {
                digits[i].drawColored(poseStack, consumer, light, overlay, 10 / 16.f, 0f, dimRi, dimGi, dimBi, ai);
            }
            for (int i = 0; i < 5; i++) {
                dots[i].drawColored(poseStack, consumer, light, overlay, dimRi, dimGi, dimBi, ai);
            }
            for (int i = 0; i < 3; i++) {
                colons[i].drawColored(poseStack, consumer, light, overlay, dimRi, dimGi, dimBi, ai);
            }
        }
    }

}

package mods.eln.sixnode.electricalwatch;

import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.six.SixNodeDescriptor;
import net.minecraft.client.Minecraft;
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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;

import static mods.eln.i18n.I18N.tr;

public class ElectricalWatchDescriptor extends SixNodeDescriptor {

    private Obj3DPart base, cHour, cMin; //Analog
    private Obj3DPart digits[] = new Obj3DPart[4]; //Digital
    private Obj3DPart dot, glass; //Digital

    enum Kind {ANALOG, DIGITAL}

    ;
    private Kind kind;
    double powerConsumtion;

    Obj3D obj;

    public ElectricalWatchDescriptor(String name, Obj3D obj, double powerConsumtion) {
        super(name, ElectricalWatchElement.class, ElectricalWatchRender.class);
        this.obj = obj;
        this.powerConsumtion = powerConsumtion;
        kind = Kind.valueOf(obj.getString("type").toUpperCase(Locale.ROOT));
        if (obj != null) {
            if (kind == Kind.ANALOG) {
                base = obj.getPart("base");
                cHour = obj.getPart("cHour");
                cMin = obj.getPart("cMin");
            } else if (kind == Kind.DIGITAL) {
                base = obj.getPart("base");
                glass = obj.getPart("glass");
                dot = obj.getPart("digitDot");
                digits[3] = obj.getPart("digit3");
                digits[2] = obj.getPart("digit2");
                digits[1] = obj.getPart("digit1");
                digits[0] = obj.getPart("digit0");
            }
        }

        voltageLevelColor = VoltageLevelColor.Neutral;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal(tr("Tells the time.")));
        tooltip.add(Component.literal(tr("Requires batteries for operation.")));
    }

    // Removed IItemRenderer methods as they are no longer supported in 1.20.x
    // Rendering should be handled by BlockEntityWithoutLevelRenderer or baked models.

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, float hour, float min, boolean isEnergyAvailable) {
        if (kind == Kind.ANALOG) {
            if (base != null) base.draw(poseStack, buffer, light, overlay);
            if (cHour != null) cHour.draw(poseStack, buffer, light, overlay, 360 * hour, -1, 0, 0);
            if (cMin != null) cMin.draw(poseStack, buffer, light, overlay, 360 * min, -1, 0, 0);
        } else if (kind == Kind.DIGITAL) {
            //Digits
            // obj.bindTexture("Digits.png");
            // mods.eln.misc.UtilsClient.INSTANCE.disableLight();
            // GL11.glColor3f(0.95f, 0.f, 0.f);
            
            int r = (int)(0.95f * 255);
            int g = 0;
            int b = 0;
            int a = 255;
            int brightLight = 15728880; // Max light

            ResourceLocation texture = digits[0].getTextureResource();
            if (texture == null) texture = new ResourceLocation("eln", "textures/missing.png");
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(texture));

            if (isEnergyAvailable) {
                int fulltimeMin = (int) (12.0f * 60.0f * hour);
                int timeHour = fulltimeMin / 60;
                int timeMin = fulltimeMin % 60;
                int tmp = timeMin % 10;
                digits[0].drawColored(poseStack, consumer, brightLight, overlay, tmp / 16.f, 0.0f, r, g, b, a);
                tmp = timeMin / 10;
                digits[1].drawColored(poseStack, consumer, brightLight, overlay, tmp / 16.f, 0.0f, r, g, b, a);
                tmp = timeHour % 10;
                digits[2].drawColored(poseStack, consumer, brightLight, overlay, tmp / 16.f, 0.0f, r, g, b, a);
                tmp = timeHour / 10;
                digits[3].drawColored(poseStack, consumer, brightLight, overlay, tmp / 16.f, 0.0f, r, g, b, a);
                if ((fulltimeMin & 0x01) != 0x00)
                    // GL11.glColor3f(0.05f, 0.f, 0.f);
                    r = (int)(0.05f * 255);
                dot.drawColored(poseStack, consumer, brightLight, overlay, r, g, b, a);
            } else {
                for (int idx = 0; idx < 4; idx++)
                    digits[idx].drawColored(poseStack, consumer, brightLight, overlay, 10.f / 16.f, 0.f, r, g, b, a);
                // GL11.glColor3f(0.05f, 0.f, 0.f);
                r = (int)(0.05f * 255);
                dot.drawColored(poseStack, consumer, brightLight, overlay, r, g, b, a);
            }
            // GL11.glColor3f(1.f, 1.f, 1.f);
            // mods.eln.misc.UtilsClient.INSTANCE.enableLight();
            //Frame
            base.draw(poseStack, buffer, light, overlay);
            //Glass (reflections)
            // mods.eln.misc.UtilsClient.INSTANCE.enableBlend();
            //UtilsClient.enableBilinear();
            // obj.bindTexture("Reflection.png");
            float rotYaw = Minecraft.getInstance().player.getYRot() / 360.f;
            float rotPitch = Minecraft.getInstance().player.getXRot() / 180.f;
            float pos = (((float) Minecraft.getInstance().player.getX()) + ((float) Minecraft.getInstance().player.getZ())) / 64.f;
            glass.draw(poseStack, buffer, light, overlay, rotYaw + pos, rotPitch * 0.875f);
            //UtilsClient.disableBilinear(); //BUG: Not always disabled.
            // mods.eln.misc.UtilsClient.INSTANCE.disableBlend();
        }
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        //Data.addSignal(newItemStack());
    }

    @Nullable
    @Override
    public mods.eln.misc.LRDU getFrontFromPlace(@NotNull mods.eln.misc.Direction side, @NotNull Player player) {
        return super.getFrontFromPlace(side, player).left();
    }
}

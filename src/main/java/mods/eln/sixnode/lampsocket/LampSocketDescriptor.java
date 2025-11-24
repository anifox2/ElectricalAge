package mods.eln.sixnode.lampsocket;

import mods.eln.misc.RealisticEnum;
import mods.eln.misc.VoltageLevelColor;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class LampSocketDescriptor extends SixNodeDescriptor {

    public LampSocketType socketType;
    LampSocketObjRender render;

    public boolean cameraOpt = true;

    public int range;
    public String modelName;
    float alphaZMin, alphaZMax, alphaZBoot;
    public boolean cableFront = true;
    public boolean cableLeft = true;
    public boolean cableRight = true;
    public boolean cableBack = true;

    public float initialRotateDeg = 0.f;
    public boolean rotateOnlyBy180Deg = false;

    public boolean paintable = false;
    public boolean renderIconInHand = false;

    public LampSocketDescriptor(String name, LampSocketObjRender render,
                                LampSocketType socketType,
                                boolean paintable,
                                int range,
                                float alphaZMin, float alphaZMax,
                                float alphaZBoot) {
        super(name, LampSocketElement.class, LampSocketRender.class);
        this.socketType = socketType;
        this.paintable = paintable;
        this.range = range;
        this.alphaZMin = alphaZMin;
        this.alphaZMax = alphaZMax;
        this.alphaZBoot = alphaZBoot;
        this.render = render;

        voltageLevelColor = VoltageLevelColor.Neutral;
    }

    public void setInitialOrientation(float rotateDeg) {
        this.initialRotateDeg = rotateDeg;
    }

    public void setUserRotationLibertyDegrees(boolean only180) {
        this.rotateOnlyBy180Deg = only180;
    }

    boolean noCameraOpt() {
        return cameraOpt;
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addLight(newItemStack());
    }

    @Override
    public void draw(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, boolean signal) {
        // TODO: Implement item rendering if needed, or delegate to render.drawItem if we have MultiBufferSource
    }

    @Override
    public boolean hasVolume() {
        return hasGhostGroup();
    }
}

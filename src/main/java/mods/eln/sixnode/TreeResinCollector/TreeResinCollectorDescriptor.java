package mods.eln.sixnode.treeresincollector;

import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.wiki.Data;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.BlockTags;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class TreeResinCollectorDescriptor extends SixNodeDescriptor {

    private Obj3D obj;
    private Obj3DPart main, fill;

    float emptyS, emptyT;

    public TreeResinCollectorDescriptor(String name, Obj3D obj) {
        super(name, TreeResinCollectorElement.class, TreeResinCollectorRender.class);
        this.obj = obj;
        if (obj != null) {
            main = obj.getPart("main");
            fill = obj.getPart("fill");
            if (fill != null) {
                emptyT = fill.getFloat("emptyT");
                emptyS = fill.getFloat("emptyS");
            }
        }

        voltageLevelColor = VoltageLevelColor.Neutral;
    }

    void draw(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, float factor) {
        if (main != null) main.draw(poseStack, buffer, light, overlay);
        if (fill != null) {
            if (factor > 1f) factor = 1f;
            factor = (1f - factor);
            
            poseStack.pushPose();
            poseStack.translate(0f, 0f, factor * emptyT);
            float scale = 1f - factor * (1f - emptyS);
            poseStack.scale(scale, scale, 1f);
            fill.draw(poseStack, buffer, light, overlay);
            poseStack.popPose();
        }
    }

    @Override
    public void setParent(Item item, int damage) {
        super.setParent(item, damage);
        Data.addMachine(newItemStack());
    }

    @Override
    public void addInformation(ItemStack itemStack, Player entityPlayer, List list, boolean par4) {
        super.addInformation(itemStack, entityPlayer, list, par4);
        Collections.addAll(list, tr("Produces Tree Resin over\ntime when put on a tree.").split("\n"));
    }

    public static boolean isWood(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    public static boolean isLeaf(BlockState state) {
        return state.is(BlockTags.LEAVES);
    }

    @Override
    public boolean canBePlacedOnSide(Player player, Coordinate c, Direction side) {
        BlockState b = c.getBlockState();
        if (!isWood(b) || side.isY()) {
            Utils.addChatMessage(player, tr("This block can only be placed on the side of a tree!"));
            return false;
        }
        return true;
    }
}

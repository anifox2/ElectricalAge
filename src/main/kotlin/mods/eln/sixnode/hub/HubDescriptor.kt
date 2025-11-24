package mods.eln.sixnode.hub

import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Obj3D
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import org.lwjgl.opengl.GL11
import mods.eln.i18n.I18N.tr
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer

class HubDescriptor(name: String, override var obj: Obj3D?) : SixNodeDescriptor(name, HubElement::class.java, HubRender::class.java) {

    var main: Obj3DPart? = null
    var connection = arrayOfNulls<Obj3DPart>(6)

    init {
        if (obj != null) {
            main = obj!!.getPart("main")
            for (idx in 0 until 6) {
                connection[idx] = obj!!.getPart("connection$idx")
            }
        }
        voltageLevelColor = VoltageLevelColor.Neutral
    }

    fun draw(poseStack: PoseStack, consumer: VertexConsumer, packedLight: Int, packedOverlay: Int, connectionGrid: BooleanArray) {
        main?.draw(poseStack, consumer, packedLight, packedOverlay)
        for (idx in 0 until 6) {
            if (connectionGrid[idx]) {
                // TODO: Set color dark
            } else {
                // TODO: Set color light
            }
            connection[idx]?.draw(poseStack, consumer, packedLight, packedOverlay)
        }
    }

    fun draw(connectionGrid: BooleanArray) {
        if (main != null) main!!.draw()
        for (idx in 0 until 6) {
            if (connectionGrid[idx])
                GL11.glColor3f(40 / 255f, 40 / 255f, 40 / 255f)
            else
                GL11.glColor3f(150 / 255f, 150 / 255f, 150 / 255f)

            if (connection[idx] != null) connection[idx]!!.draw()
        }
        GL11.glColor3f(1f, 1f, 1f)
    }

    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addWiring(newItemStack())
    }

    override fun appendHoverText(itemStack: ItemStack, level: Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        tr("Used to split a cable in 5 directions").split("\n").forEach { list.add(Component.literal(it)) }
    }

    override fun getFrontFromPlace(side: Direction, player: Player): LRDU {
        return LRDU.Up
    }
}

package mods.eln.sixnode.powersocket

import mods.eln.i18n.I18N.tr
import mods.eln.misc.*
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.misc.UtilsClient.setGlColorFromDye
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import org.lwjgl.opengl.GL11

class PowerSocketDescriptor(subID: Int, name: String, obj: Obj3D) :
    SixNodeDescriptor(name, PowerSocketElement::class.java, PowerSocketRender::class.java) {
    private var base: Obj3DPart? = null
    private var socket: Obj3DPart? = null

    init {
        base = obj.getPart("SocketBase")
        when (subID) {
            1 -> {
                socket = obj.getPart("Socket50V") // Type J socket - 10 amps specification
                voltageLevelColor = VoltageLevelColor.Neutral
            }

            2 -> {
                socket = obj.getPart("Socket200V") // Type E socket - 16 amps specification
                voltageLevelColor = VoltageLevelColor.Neutral
            }
            else -> socket = null
        }
    }

    @JvmOverloads
    fun draw(color: Int = 0) {
        if (base != null) base!!.draw()
        if (socket != null) {
            setGlColorFromDye(color, 0.7f, 0.3f)
            socket!!.draw()
            GL11.glColor3f(1f, 1f, 1f)
        }
    }

    // ...existing code...
    override fun appendHoverText(itemStack: ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        val text = tr("Supplies any device\nplugged in with energy.").split("\n")
        text.forEach { list.add(Component.literal(it)) }
    }

    override fun getFrontFromPlace(side: Direction, player: Player): LRDU? {
        return LRDU.Down
    }
}

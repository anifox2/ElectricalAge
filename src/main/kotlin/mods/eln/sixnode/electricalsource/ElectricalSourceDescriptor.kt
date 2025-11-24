package mods.eln.sixnode.electricalsource

import mods.eln.Eln
import mods.eln.misc.Obj3D
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.misc.RealisticEnum
import mods.eln.misc.Utils
import mods.eln.misc.UtilsClient
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.six.SixNodeDescriptor
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL11
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import java.util.List

class ElectricalSourceDescriptor(
    name: String,
    override var obj: Obj3D?,
    var signalSource: Boolean
) : SixNodeDescriptor(name, ElectricalSourceElement::class.java, ElectricalSourceRender::class.java) {

    var main: Obj3DPart? = null
    var led: Obj3DPart? = null

    init {
        if (obj != null) {
            main = obj!!.getPart("main")
            led = obj!!.getPart("led")
        }

        if (signalSource) {
            voltageLevelColor = VoltageLevelColor.SignalVoltage
        } else {
            voltageLevelColor = VoltageLevelColor.Neutral
        }
    }

    fun draw(ledOn: Boolean) {
        main?.draw()
        if (led != null) {
            if (ledOn)
                UtilsClient.drawLight(led!!)
            else {
                GL11.glPushMatrix()
                GL11.glColor3f(0.1f, 0.1f, 0.1f)
                led!!.draw()
                GL11.glPopMatrix()
            }
        }
    }

    override fun draw(poseStack: PoseStack, consumer: VertexConsumer, packedLight: Int, packedOverlay: Int, signal: Boolean) {
        if (signalSource) {
            if (signal) obj!!.draw("on", poseStack, consumer, packedLight, packedOverlay)
            else obj!!.draw("off", poseStack, consumer, packedLight, packedOverlay)
            obj!!.draw("main", poseStack, consumer, packedLight, packedOverlay)
        } else {
            obj!!.draw(poseStack, consumer, packedLight, packedOverlay)
        }
    }

    override fun addInformation(itemStack: ItemStack, player: Player?, list: MutableList<String>, par4: Boolean) {
        super.addInformation(itemStack, player, list, par4)
        list.addAll("Provides an ideal voltage source\nwithout energy or power limitation.".split("\n"))
        list.add("")
        list.add("Internal resistance: ${Utils.plotValue(Eln.instance!!.lowVoltageCableDescriptor!!.electricalRs)}\u2126")
        list.add("")
        list.add("Creative block.")
    }

    override fun addRealismContext(list: MutableList<String>): RealisticEnum {
        super.addRealismContext(list)
        list.add("Acts as an ideal voltage source, with a small inline resistance")
        return RealisticEnum.IDEAL
    }
}

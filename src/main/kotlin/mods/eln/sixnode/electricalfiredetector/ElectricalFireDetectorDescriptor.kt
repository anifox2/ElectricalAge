package mods.eln.sixnode.electricalfiredetector

import mods.eln.i18n.I18N.tr
import mods.eln.misc.Obj3D
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.misc.Utils
import mods.eln.misc.UtilsClient
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL11
import java.util.Collections

class ElectricalFireDetectorDescriptor(
    name: String,
    obj: Obj3D?,
    var maxRange: Double,
    var batteryPowered: Boolean
) : SixNodeDescriptor(name, ElectricalFireDetectorElement::class.java, ElectricalFireDetectorRender::class.java) {

    private var detector: Obj3DPart? = null
    private var led: Obj3DPart? = null
    var pinDistance: FloatArray? = null
    val updateInterval = 0.5

    init {
        if (obj != null) {
            detector = obj.getPart("Detector")
            led = obj.getPart("Led")
            pinDistance = Utils.getSixNodePinDistance(detector)
        }

        if (batteryPowered) {
            voltageLevelColor = VoltageLevelColor.Neutral
        } else {
            voltageLevelColor = VoltageLevelColor.SignalVoltage
        }
    }

    fun draw(firePresent: Boolean) {
        detector?.draw()
        if (led != null) {
            if (firePresent) {
                UtilsClient.drawLight(led!!)
            } else {
                GL11.glColor3f(0.5f, 0.5f, 0.5f)
                led!!.draw()
                GL11.glColor3f(1f, 1f, 1f)
            }
        }
    }


    companion object {
        const val PowerComsumption = 20000.0 / (3600 * 40)
    }
}

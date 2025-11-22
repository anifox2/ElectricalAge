package mods.eln.sixnode.lampsocket

import mods.eln.misc.VoltageLevelColor
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class LampSocketDescriptor(
    name: String,
    var render: LampSocketObjRender,
    var socketType: LampSocketType,
    var paintable: Boolean,
    var range: Int,
    var alphaZMin: Float,
    var alphaZMax: Float,
    var alphaZBoot: Float
) : SixNodeDescriptor(name, LampSocketElement::class.java, LampSocketRender::class.java) {

    var cameraOpt = true
    var modelName: String? = null
    var cableFront = true
    var cableLeft = true
    var cableRight = true
    var cableBack = true

    var initialRotateDeg = 0f
    var rotateOnlyBy180Deg = false

    var renderIconInHand = false

    init {
        voltageLevelColor = VoltageLevelColor.Neutral
    }

    fun setInitialOrientation(rotateDeg: Float) {
        this.initialRotateDeg = rotateDeg
    }

    fun setUserRotationLibertyDegrees(only180: Boolean) {
        this.rotateOnlyBy180Deg = only180
    }

    fun noCameraOpt(): Boolean {
        return cameraOpt
    }
}

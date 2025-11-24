package mods.eln.transparentnode.waterturbine

import mods.eln.Eln
import mods.eln.misc.Coordinate
import mods.eln.misc.Obj3D
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item

class WaterTurbineDescriptor(
    name: String?,
    override var obj: Obj3D?,
    var cable: ElectricalCableDescriptor,
    var nominalPower: Double,
    var maxVoltage: Double,
    var waterCoord: Coordinate,
    var soundName: String,
    var nominalVolume: Float
) : TransparentNodeDescriptor(
    name,
    WaterTurbineElement::class.java,
    WaterTurbineRender::class.java
) {
    var wheel: Obj3D.Obj3DPart? = null
    var support: Obj3D.Obj3DPart? = null
    var generator: Obj3D.Obj3DPart? = null
    var speed: Float = 0f

    init {
        if (obj != null) {
            wheel = obj!!.getPart("Wheel")
            support = obj!!.getPart("Support")
            generator = obj!!.getPart("Generator")
            speed = 60f
        }
        voltageLevelColor = VoltageLevelColor.LowVoltage
        Data.addEnergy(newItemStack())
    }

    fun draw(alpha: Float) {
        support?.draw()
        generator?.draw()
        wheel?.draw(alpha, 1f, 0f, 0f)
    }

    fun draw(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int, alpha: Float) {
        support?.draw(poseStack, bufferSource, packedLight, packedOverlay)
        generator?.draw(poseStack, bufferSource, packedLight, packedOverlay)
        if (wheel != null) {
            poseStack.pushPose()
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(alpha))
            wheel!!.draw(poseStack, bufferSource, packedLight, packedOverlay)
            poseStack.popPose()
        }
    }
}


package mods.eln.transparentnode.heatfurnace

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeRender
import mods.eln.misc.Obj3D
import mods.eln.sim.ThermalLoadInitializerByPowerDrop
import mods.eln.Eln
import org.lwjgl.opengl.GL11
import mods.eln.misc.UtilsClient
import com.mojang.math.Axis

class HeatFurnaceDescriptor(
    name: String,
    val objName: String,
    val maxPower: Double,
    val energyPerItem: Double,
    val burnTime: Int,
    val maxTemperature: Double,
    val thermalLoadInit: ThermalLoadInitializerByPowerDrop,
    override var obj: Obj3D? = null
) : TransparentNodeDescriptor(
    name,
    HeatFurnaceElement::class.java,
    HeatFurnaceRender::class.java
) {
    var main: Obj3D.Obj3DPart? = null
    var door: Obj3D.Obj3DPart? = null

    init {
        if (obj == null) {
            obj = Eln.obj.getObj(objName)
        }
        main = obj?.getPart("Main")
        door = obj?.getPart("Door")
    }

    fun draw(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int, doorAngle: Float) {
        main?.draw(poseStack, bufferSource, packedLight, packedOverlay)
        
        if (door != null) {
            poseStack.pushPose()
            // Rotate door. Assuming hinge is on the left (-X) and door is on front (-Z or +Z depending on rotation).
            // Since we don't know the exact pivot, we'll try a reasonable guess or just rotate.
            // If the model origin is (0,0,0), rotating around Y will rotate around the center of the block.
            // We'll assume the door part origin is set correctly in the OBJ or we rotate around center.
            poseStack.mulPose(Axis.YP.rotationDegrees(doorAngle * 90f))
            door?.draw(poseStack, bufferSource, packedLight, packedOverlay)
            poseStack.popPose()
        }
    }
}

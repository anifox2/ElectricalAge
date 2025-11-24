package mods.eln.transparentnode.heatfurnace

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeRender
import mods.eln.misc.Obj3D
import mods.eln.sim.ThermalLoadInitializer

class HeatFurnaceDescriptor(
    name: String,
    val objName: String,
    val val1: Double,
    val val2: Double,
    val val3: Int,
    val val4: Double,
    val thermalLoad: ThermalLoadInitializer,
    override var obj: Obj3D? = null
) : TransparentNodeDescriptor(
    name,
    HeatFurnaceElement::class.java,
    HeatFurnaceRender::class.java
) {
    fun draw(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        obj?.draw(poseStack, bufferSource, packedLight, packedOverlay)
    }
}

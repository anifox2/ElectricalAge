package mods.eln.transparentnode.solarpanel

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import mods.eln.cable.CableRenderDescriptor
import mods.eln.ghost.GhostGroup
import mods.eln.misc.Coordinate
import mods.eln.misc.Obj3D
import mods.eln.node.transparent.TransparentNodeDescriptor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

import mods.eln.misc.Direction
import net.minecraft.world.entity.LivingEntity

class SolarPanelDescriptor(
    name: String,
    override var obj: Obj3D?,
    var cableRender: CableRenderDescriptor?,
    ghostGroup: GhostGroup,
    var w: Int,
    var h: Int,
    var l: Int,
    var groundCoordinate: Coordinate?,
    var nominalVoltage: Double,
    var nominalPower: Double,
    var rs: Double,
    var minAngle: Double,
    var maxAngle: Double
) : TransparentNodeDescriptor(
    name,
    SolarPanelElement::class.java,
    SolarPanelRender::class.java
) {
    var main: Obj3D.Obj3DPart? = null
    var panel: Obj3D.Obj3DPart? = null

    init {
        this.ghostGroup = ghostGroup
        if (obj != null) {
            main = obj!!.getPart("main")
            panel = obj!!.getPart("panel")
        }
    }

    override fun draw(poseStack: PoseStack, consumer: VertexConsumer, packedLight: Int, packedOverlay: Int) {
        // Apply scaling for item rendering
        poseStack.pushPose()
        
        // Use main object for scaling calculations if available, otherwise fallback to obj
        val refObj = if (main != null) obj!! else obj
        
        if (refObj != null) {
            var factor = refObj.yDim * 0.6f
            factor = factor.coerceAtLeast((refObj.zMax.coerceAtLeast(-refObj.xMin) + Math.max(refObj.xMax, -refObj.zMin)) * 0.7f)
            factor = 1f / factor
            
            poseStack.scale(factor, factor, factor)
            val tx = (refObj.zMin.coerceAtMost(refObj.xMin) + refObj.xMax.coerceAtLeast(refObj.zMax)) / 2 - (refObj.xMax + refObj.xMin) / 2
            val ty = 1.0f - (refObj.xMax + refObj.xMin) / 2 - (refObj.zMax + refObj.zMin) / 2 - (refObj.yMax + refObj.yMin) / 2
            poseStack.translate(tx.toDouble(), ty.toDouble(), 0.0)
        }

        if (main != null) main!!.draw(poseStack, consumer, packedLight, packedOverlay)
        if (panel != null) panel!!.draw(poseStack, consumer, packedLight, packedOverlay)
        
        poseStack.popPose()
    }

    override fun getFrontFromPlace(side: Direction, entityLiving: LivingEntity?): Direction? {
        if (minAngle != maxAngle && groundCoordinate != null) {
            // That is, if this isn't a 1x1 panel.
            return Direction.ZN
        } else {
            return super.getFrontFromPlace(side, entityLiving)
        }
    }
}

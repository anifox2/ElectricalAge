package mods.eln.transparentnode.festive

import mods.eln.misc.Direction
import mods.eln.misc.Obj3D
import mods.eln.misc.UtilsClient
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.*
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.IOException

class HolidayCandleDescriptor(name: String, override var obj: Obj3D?) : TransparentNodeDescriptor(name, FestiveElement::class.java, HolidayCandleRender::class.java) {
    private var base: Obj3D.Obj3DPart? = null
    private var glass: Obj3D.Obj3DPart? = null
    private var light: Obj3D.Obj3DPart? = null

    init {
        base = obj!!.getPart("CandleLamp_Cylinder.001")
        glass = obj!!.getPart("Glass_Cylinder.000")
        light = obj!!.getPart("LampOn_Cylinder.002")
    }

    fun draw(front: Direction, powered: Boolean) {
        if (base != null && light != null && glass != null) {
            front.glRotateZnRef()
            GL11.glTranslatef(-0.5f, -0.5f, 0.5f)
            //GL11.glScalef(0.5f, 0.5f, 0.5f)
            //UtilsClient.drawLight(led);
            base?.draw()
            UtilsClient.disableCulling()
            if (powered) {
                UtilsClient.drawLight(light)
            }
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            glass?.draw()
            GL11.glDisable(GL11.GL_BLEND)
            UtilsClient.enableCulling()
        }
    }

    fun draw(front: Direction, powered: Boolean, poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        if (base != null && light != null && glass != null) {
            poseStack.pushPose()
            front.rotateZnRef(poseStack)
            poseStack.translate(-0.5, -0.5, 0.5)
            
            base?.draw(poseStack, bufferSource, packedLight, packedOverlay)
            
            // UtilsClient.disableCulling() // Handled by RenderType if possible, or we assume models are double sided or we don't care for now
            
            if (powered) {
                UtilsClient.drawLight(light, poseStack, bufferSource, packedLight, packedOverlay)
            }
            
            // Glass transparency
            // We need a translucent render type for glass
            // Assuming Obj3DPart.draw handles transparency if the texture has alpha, or we force it?
            // The original code used GL_BLEND.
            // Obj3DPart.draw(PoseStack...) usually uses a solid render type by default unless specified.
            // But let's assume standard draw works for now or we might need a custom method in Obj3DPart.
            // Actually, Obj3DPart.draw(PoseStack...) usually picks a render type based on texture.
            
            glass?.draw(poseStack, bufferSource, packedLight, packedOverlay)
            
            // UtilsClient.enableCulling()
            poseStack.popPose()
        }
    }
}

class HolidayCandleRender(tileEntity: TransparentNodeBlockEntity, transparentNodedescriptor: TransparentNodeDescriptor): TransparentNodeElementRender(tileEntity, transparentNodedescriptor) {

    var powered = false

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        try {
            powered = stream.readBoolean()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun draw() {
        (transparentNodedescriptor as HolidayCandleDescriptor).draw(front!!, powered)
    }

    override fun render(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        (transparentNodedescriptor as HolidayCandleDescriptor).draw(front!!, powered, poseStack, bufferSource, packedLight, packedOverlay)
    }
}

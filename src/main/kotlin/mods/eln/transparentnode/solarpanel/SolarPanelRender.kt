package mods.eln.transparentnode.solarpanel

import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import org.lwjgl.opengl.GL11

class SolarPanelRender(
    tileEntity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(tileEntity, descriptor) {

    override fun draw() {
        val descriptor = transparentNodedescriptor as SolarPanelDescriptor
        
        front?.glRotateXnRef()

        descriptor.main?.draw()

        val world = tileEntity.level ?: return
        val time = world.dayTime % 24000
        
        // 0 = Sunrise, 6000 = Noon, 12000 = Sunset
        // Map 0..12000 to -90..90 degrees
        var angle = (time - 6000) / 6000f * 90f
        
        // Clamp for night time
        if (time > 12000) {
            angle = -90f // Reset to sunrise position
        } else {
            if (angle < -90f) angle = -90f
            if (angle > 90f) angle = 90f
        }

        if (descriptor.panel != null) {
            GL11.glPushMatrix()
            GL11.glRotatef(angle, 1f, 0f, 0f)
            descriptor.panel!!.draw()
            GL11.glPopMatrix()
        }
    }
}

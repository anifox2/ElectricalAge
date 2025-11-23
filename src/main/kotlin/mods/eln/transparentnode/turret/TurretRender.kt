package mods.eln.transparentnode.turret

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.transparentnode.turret.TurretElement.TurretMechanicsSimulation
import org.lwjgl.opengl.GL11

class TurretRender(
    entity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {

    private val descriptor: TurretDescriptor = descriptor as TurretDescriptor
    private val simulation = TurretMechanicsSimulation(this.descriptor)

    override fun draw() {
        front?.glRotateXnRef()
        
        descriptor.base?.draw()
        
        GL11.glPushMatrix()
        GL11.glRotatef(simulation.getTurretAngle(), 0f, 1f, 0f)
        descriptor.turret?.draw()
        
        GL11.glPushMatrix()
        // Gun translation and rotation logic would go here
        // Simplified for now
        GL11.glRotatef(simulation.getGunElevation(), 1f, 0f, 0f)
        descriptor.gun?.draw()
        
        GL11.glPopMatrix()
        GL11.glPopMatrix()
    }

    override fun refresh(deltaT: Float) {
        simulation.process(deltaT.toDouble())
    }
}

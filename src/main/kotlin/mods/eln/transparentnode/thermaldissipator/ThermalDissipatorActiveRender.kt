package mods.eln.transparentnode.thermaldissipator

import mods.eln.misc.RcInterpolator
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeEntity
import java.io.DataInputStream

class ThermalDissipatorActiveRender(
    entity: TransparentNodeEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {

    private val descriptor: ThermalDissipatorActiveDescriptor = descriptor as ThermalDissipatorActiveDescriptor
    private val rc = RcInterpolator(2f)
    private var alpha = 0f
    private var powerFactor = 0f

    override fun draw() {
        super.draw()
        front.glRotateXnRef()
        descriptor.draw(alpha)
    }

    override fun refresh(deltaT: Float) {
        rc.setTarget(powerFactor)
        rc.step(deltaT)
        alpha += rc.get() * 360f * deltaT
        while (alpha > 360f) alpha -= 360f
    }

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        try {
            powerFactor = stream.readFloat()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

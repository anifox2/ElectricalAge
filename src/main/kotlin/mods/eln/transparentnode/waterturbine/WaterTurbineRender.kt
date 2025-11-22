package mods.eln.transparentnode.waterturbine

import mods.eln.misc.Coordinate
import mods.eln.misc.RcInterpolator
import mods.eln.misc.Utils
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeEntity

class WaterTurbineRender(
    entity: TransparentNodeEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {

    private val descriptor: WaterTurbineDescriptor = descriptor as WaterTurbineDescriptor
    private var waterCoord: Coordinate? = null
    private var waterCoordRight: Coordinate? = null
    private val powerFactorFilter = RcInterpolator(1f)
    private val dirFilter = RcInterpolator(0.5f)
    private var alpha = (Math.random() * 360).toFloat()
    private var soundPlaying = false

    override fun draw() {
        front?.glRotateXnRef()
        descriptor.draw(alpha)
    }

    override fun refresh(deltaT: Float) {
        // Animation logic would go here
        // For now, just increment alpha
        alpha += deltaT * 10f // Dummy rotation
    }
}

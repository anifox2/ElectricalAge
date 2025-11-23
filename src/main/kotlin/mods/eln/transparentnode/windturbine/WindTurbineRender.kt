package mods.eln.transparentnode.windturbine

import mods.eln.cable.CableRenderType
import mods.eln.misc.Direction
import mods.eln.misc.LRDUMask
import mods.eln.misc.RcInterpolator
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity
import java.util.Random

class WindTurbineRender(
    entity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {

    private val descriptor: WindTurbineDescriptor = descriptor as WindTurbineDescriptor
    private var haloBlink_OnTime = 0.5f
    private var haloBlink_OffTime = 2.5f

    private var soundPlaying = false
    private var haloBlinkCounter = 0f
    private var haloState = false

    private var renderPreProcess: CableRenderType? = null
    private val eConn = LRDUMask()

    private val powerFactorFilter = RcInterpolator(2.0f)
    private var alpha = (Math.random() * 360).toFloat()

    private var wind = 0f
    private var powerFactor = 0f

    init {
        val rand = Random()
        this.haloBlinkCounter = rand.nextFloat() * this.haloBlink_OffTime
        this.haloBlink_OffTime += rand.nextFloat() * this.haloBlink_OffTime / 15f
        this.haloBlink_OnTime += rand.nextFloat() * this.haloBlink_OnTime / 15f
    }

    override fun draw() {
        renderPreProcess = drawCable(Direction.YN, descriptor.cable.render, eConn, renderPreProcess, false)
        front?.glRotateXnRef()
        descriptor.draw(alpha)
    }

    override fun refresh(deltaT: Float) {
        super.refresh(deltaT)
        alpha += deltaT * 10f
    }
}

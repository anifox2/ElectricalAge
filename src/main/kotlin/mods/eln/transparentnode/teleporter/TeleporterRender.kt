package mods.eln.transparentnode.teleporter

import mods.eln.misc.Coordinate
import mods.eln.misc.PhysicalInterpolator
import mods.eln.misc.RcInterpolator
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeBlockEntity
import java.io.DataInputStream

class TeleporterRender(
    entity: TransparentNodeBlockEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {

    private val d: TeleporterDescriptor = descriptor as TeleporterDescriptor
    private var doorState = false
    private val doorInterpolator = PhysicalInterpolator(0.2f, 8.0f, 5f, 0.0f)
    private val processRatioInterpolator = RcInterpolator(1f)
    private val blueInterpolator = RcInterpolator(0.5f)
    private var gyroAlpha = 0f

    init {
        //doorInterpolator.setMaxSpeed(0.3f)
    }

    override fun draw() {
        
        front!!.glRotateXnRef()
        // Drawing logic
        d.main?.draw()
    }

    override fun render(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        front?.rotateXnRef(poseStack)
        d.main?.draw(poseStack, bufferSource, packedLight, packedOverlay)
    }

    override fun refresh(deltaT: Float) {
        doorInterpolator.step(deltaT)
        processRatioInterpolator.step(deltaT)
        blueInterpolator.step(deltaT)
        gyroAlpha += deltaT * 100f
    }

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        try {
            doorState = stream.readBoolean()
            // Read other states
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

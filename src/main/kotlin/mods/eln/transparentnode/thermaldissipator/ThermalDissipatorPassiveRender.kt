package mods.eln.transparentnode.thermaldissipator

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.node.transparent.TransparentNodeEntity

class ThermalDissipatorPassiveRender(
    entity: TransparentNodeEntity,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElementRender(entity, descriptor) {

    private val descriptor: ThermalDissipatorPassiveDescriptor = descriptor as ThermalDissipatorPassiveDescriptor

    override fun draw() {
        super.draw()
        front.glRotateXnRef()
        descriptor.draw()
    }
}

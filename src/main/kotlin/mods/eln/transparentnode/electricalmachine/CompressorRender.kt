package mods.eln.transparentnode.electricalmachine

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeEntity

class CompressorRender(
    entity: TransparentNodeEntity,
    descriptor: TransparentNodeDescriptor
) : ElectricalMachineRender(entity, descriptor) {
    override fun draw() {
        super.draw()
        // Add drawing logic
    }
}

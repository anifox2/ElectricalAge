package mods.eln.cable

import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableElement
import mods.eln.sixnode.electricalcable.ElectricalCableRender

class CopperCableDescriptor(name: String) : ElectricalCableDescriptor(
    name,
    CableRenderDescriptor("eln", "textures/blocks/lowvoltagecable.png", 4f, 4f),
    "Copper Cable",
    false
) {
    init {
        electricalRp = 0.01
        electricalMaximalPower = 100.0
    }
}

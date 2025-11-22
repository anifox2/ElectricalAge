package mods.eln.transparentnode.solarpanel

import mods.eln.cable.CableRenderDescriptor
import mods.eln.ghost.GhostGroup
import mods.eln.misc.Coordinate
import mods.eln.misc.Obj3D
import mods.eln.node.transparent.TransparentNodeDescriptor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class SolarPanelDescriptor(
    name: String,
    var obj: Obj3D,
    var cableRender: CableRenderDescriptor?,
    ghostGroup: GhostGroup,
    var w: Int,
    var h: Int,
    var l: Int,
    var groundCoordinate: Coordinate?,
    var nominalVoltage: Double,
    var nominalPower: Double,
    var rs: Double,
    var minAngle: Double,
    var maxAngle: Double
) : TransparentNodeDescriptor(
    name,
    SolarPanelElement::class.java,
    SolarPanelRender::class.java
) {
    init {
        this.ghostGroup = ghostGroup
    }
}

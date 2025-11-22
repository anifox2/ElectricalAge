package mods.eln.transparentnode.powercapacitor

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeRender
import mods.eln.misc.Obj3D
import mods.eln.misc.SeriesFunction

class PowerCapacitorDescriptor(
    name: String,
    val obj: Obj3D?,
    val series: SeriesFunction,
    val voltage: Double
) : TransparentNodeDescriptor(
    name,
    PowerCapacitorElement::class.java,
    PowerCapacitorRender::class.java
) {
}

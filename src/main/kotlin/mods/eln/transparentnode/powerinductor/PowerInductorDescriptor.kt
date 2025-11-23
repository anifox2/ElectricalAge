package mods.eln.transparentnode.powerinductor

import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeRender
import mods.eln.misc.Obj3D
import mods.eln.misc.SeriesFunction

class PowerInductorDescriptor(
    name: String,
    val obj: Obj3D?,
    val series: SeriesFunction
) : TransparentNodeDescriptor(
    name,
    PowerInductorElement::class.java,
    PowerInductorRender::class.java
) {
    init {
    }
}

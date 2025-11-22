package mods.eln.transparentnode

import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNode
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.misc.Coordinate

class LampSupplyElement(node: TransparentNode?, descriptor: TransparentNodeDescriptor) : TransparentNodeElement(node, descriptor) {
    var range = 0.0
    var powerLoad = NbtElectricalLoad("powerLoad")
    
    val coordinate: Coordinate
        get() = transparentNode!!.coordinate

    override fun initialize() {
    }
    
    fun getChannelState(id: Int): Boolean = false

    class PowerSupplyChannelHandle(val channel: String, val element: LampSupplyElement, val id: Int) {
        fun getU(): Double = 0.0
    }
    
    companion object {
        val channelMap = mutableMapOf<String, List<PowerSupplyChannelHandle>>()
    }
}

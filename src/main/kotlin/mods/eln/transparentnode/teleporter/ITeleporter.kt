package mods.eln.transparentnode.teleporter

import mods.eln.misc.Coordinate

interface ITeleporter {
    fun getTeleportCoordonate(): Coordinate
    fun getName(): String
    fun reservate(): Boolean
    fun reservateRefresh(doorState: Boolean, processRatio: Float)
}

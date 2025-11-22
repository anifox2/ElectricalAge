package mods.eln.integration.waila

import mods.eln.misc.Coordinate
import java.util.HashMap

object WailaCache {
    val ghostNodes = HashMap<Coordinate, GhostNodeWailaData>()
    val sixNodes = HashMap<SixNodeCoordonate, SixNodeWailaData>()
    val nodes = HashMap<Coordinate, Map<String, String>>()
}

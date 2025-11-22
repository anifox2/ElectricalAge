package mods.eln

object Other {
    const val modIdIc2 = "IC2"
    const val modIdTe = "ThermalExpansion"
    const val modIdOc = "OpenComputers"

    fun isIc2Loaded(): Boolean = false
    fun isTeLoaded(): Boolean = false
    fun isOcLoaded(): Boolean = false

    fun getWattsToEu(): Double = 0.25
    fun getWattsToRf(): Double = 1.0
    fun getWattsToOC(): Double = 1.0
}

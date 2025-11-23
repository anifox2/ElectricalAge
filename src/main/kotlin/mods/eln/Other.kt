package mods.eln

object Other {
    const val modIdIc2 = "IC2"
    const val modIdTe = "ThermalExpansion"
    const val modIdOc = "OpenComputers"

    fun isIc2Loaded(): Boolean = false
    fun isTeLoaded(): Boolean = false
    fun isOcLoaded(): Boolean = false

    var wattsToEu: Double = 0.25
    var wattsToRf: Double = 1.0
    var wattsToOC: Double = 1.0
}

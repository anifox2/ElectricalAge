package mods.eln.item

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.sim.ElectricalLoad
import kotlin.math.sqrt

class HeatingCorpElement(
    name: String,
    var electricalNominalU: Double,
    var electricalNominalP: Double,
    var electricalMaximalP: Double,
    var cableDescriptorProvider: () -> ElectricalCableDescriptor
) : GenericItemUsingDamageDescriptor(name) {

    var electricalR: Double = electricalNominalU * electricalNominalU / electricalNominalP
    var Umax: Double = sqrt(electricalMaximalP * electricalR)

    fun applyTo(load: ElectricalLoad) {
        cableDescriptorProvider().applyTo(load)
    }
}

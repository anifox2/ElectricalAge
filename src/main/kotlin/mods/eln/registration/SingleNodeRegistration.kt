package mods.eln.registration

import mods.eln.Eln
import mods.eln.node.NodeManager
import mods.eln.simplenode.DeviceProbeNode
import mods.eln.simplenode.energyconverter.EnergyConverterElnToOtherNode

object SingleNodeRegistration {

    fun registerSingle() {
        registerEnergyConverter()
        registerDeviceProbe()
    }

    private fun registerEnergyConverter() {
        if (Eln.instance!!.ElnToOtherEnergyConverterEnable) {
             NodeManager.registerUuid(
                EnergyConverterElnToOtherNode.nodeUuidStatic,
                EnergyConverterElnToOtherNode::class.java
            )
        }
    }

    private fun registerDeviceProbe() {
        NodeManager.registerUuid(
            DeviceProbeNode.getNodeUuidStatic(),
            DeviceProbeNode::class.java
        )
    }
}
package mods.eln.sixnode.electricalcable

import mods.eln.cable.CableRenderDescriptor
import mods.eln.misc.Utils
import mods.eln.sixnode.genericcable.GenericCableDescriptor
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.misc.MnaConst
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import mods.eln.i18n.I18N.tr
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

open class ElectricalCableDescriptor(
    name: String,
    render: CableRenderDescriptor,
    var description: String,
    var signalWire: Boolean
) : GenericCableDescriptor(name, ElectricalCableElement::class.java, ElectricalCableRender::class.java) {

    var electricalNominalPowerDropFactor: Double = 0.0
    var electricalRp: Double = Double.POSITIVE_INFINITY
    var electricalRsPerCelcius: Double = 0.0
    var dielectricBreakOhmPerVolt: Double = 0.0
    var dielectricBreakOhm: Double = Double.POSITIVE_INFINITY
    var dielectricVoltage: Double = Double.POSITIVE_INFINITY
    var dielectricBreakOhmMin: Double = Double.POSITIVE_INFINITY
    // var electricalNominalVoltage: Double = 0.0 // Inherited
    // var electricalNominalPower: Double = 0.0 // Inherited
    // var electricalMaximalVoltage: Double = 0.0 // Inherited
    var electricalMaximalPower: Double = 0.0

    init {
        this.render = render
        this.thermalRp = 1.0
        this.thermalRs = 1.0
        this.thermalC = 1.0
        this.thermalWarmLimit = 100.0
        this.thermalCoolLimit = -100.0
    }

    override fun applyTo(electricalLoad: ElectricalLoad, rsFactor: Double) {
        // TODO: Implement
    }

    override fun applyTo(electricalLoad: ElectricalLoad) {
        // TODO: Implement
    }

    override fun applyTo(resistor: Resistor) {
        // TODO: Implement
    }

    override fun applyTo(resistor: Resistor, factor: Double) {
        // TODO: Implement
    }

    override fun applyTo(thermalLoad: ThermalLoad) {
        // TODO: Implement
    }

    fun setPhysicalConstantLikeNormalCable(
        electricalNominalVoltage: Double, electricalNominalPower: Double, electricalNominalPowerDropFactor: Double,
        electricalMaximalVoltage: Double, electricalMaximalPower: Double,
        electricalOverVoltageStartPowerLost: Double,
        thermalWarmLimit: Double, thermalCoolLimit: Double,
        thermalNominalHeatTime: Double, thermalConductivityTao: Double
    ) {
        this.electricalNominalVoltage = electricalNominalVoltage
        this.electricalNominalPower = electricalNominalPower
        this.electricalNominalPowerDropFactor = electricalNominalPowerDropFactor

        this.thermalWarmLimit = thermalWarmLimit
        this.thermalCoolLimit = thermalCoolLimit
        this.electricalMaximalVoltage = electricalMaximalVoltage

        electricalRp = MnaConst.highImpedance
        val electricalNorminalI = electricalNominalPower / electricalNominalVoltage
        electricalRs = (electricalNominalPower * electricalNominalPowerDropFactor) / electricalNorminalI / electricalNorminalI / 2
        //electricalC = Eln.simulator.getMinimalElectricalC(electricalNominalRs, electricalRp);

        val electricalNominalPowerCalc = electricalMaximalPower / electricalNominalVoltage
        val thermalMaximalPowerDissipated = electricalNominalPowerCalc * electricalNominalPowerCalc * electricalRs * 2
        thermalC = thermalMaximalPowerDissipated * thermalNominalHeatTime / (thermalWarmLimit)
        thermalRp = thermalWarmLimit / thermalMaximalPowerDissipated
        thermalRs = thermalConductivityTao / thermalC / 2

        mods.eln.Eln.simulator.checkThermalLoad(thermalRs, thermalRp, thermalC)

        electricalRsPerCelcius = 0.0

        dielectricBreakOhmPerVolt = 0.95
    }
}
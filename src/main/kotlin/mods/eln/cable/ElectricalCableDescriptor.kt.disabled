package mods.eln.cable

import mods.eln.Eln
import mods.eln.i18n.I18N.tr
import mods.eln.misc.RealisticEnum
import mods.eln.misc.Utils
import mods.eln.misc.VoltageLevelColor
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.Resistor
import mods.eln.sim.mna.misc.MnaConst
import mods.eln.sixnode.electricalcable.ElectricalCableElement
import mods.eln.sixnode.electricalcable.ElectricalCableRender
import mods.eln.sixnode.genericcable.GenericCableDescriptor
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.util.Collections

class ElectricalCableDescriptor(
    name: String,
    render: CableRenderDescriptor?,
    var description: String,
    var signalWire: Boolean
) : GenericCableDescriptor(name, ElectricalCableElement::class.java, ElectricalCableRender::class.java) {

    var electricalNominalPowerDropFactor = 0.0
    var electricalRp = Double.POSITIVE_INFINITY
    var electricalRsPerCelcius = 0.0
    var dielectricBreakOhmPerVolt = 0.0
    var dielectricBreakOhm = Double.POSITIVE_INFINITY
    var dielectricVoltage = Double.POSITIVE_INFINITY
    var dielectricBreakOhmMin = Double.POSITIVE_INFINITY

    init {
        thermalRp = 1.0
        thermalRs = 1.0
        thermalC = 1.0
        this.render = render
        this.thermalWarmLimit = 100.0
        this.thermalCoolLimit = -100.0
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
        
        val current = electricalMaximalPower / electricalNominalVoltage
        val thermalMaximalPowerDissipated = current * current * electricalRs * 2
        thermalC = thermalMaximalPowerDissipated * thermalNominalHeatTime / (thermalWarmLimit)
        thermalRp = thermalWarmLimit / thermalMaximalPowerDissipated
        thermalRs = thermalConductivityTao / thermalC / 2

        Eln.simulator.checkThermalLoad(thermalRs, thermalRp, thermalC)

        electricalRsPerCelcius = 0.0

        dielectricBreakOhmPerVolt = 0.95
        dielectricBreakOhm = electricalMaximalVoltage * electricalMaximalVoltage / electricalOverVoltageStartPowerLost
        dielectricVoltage = electricalMaximalVoltage
        dielectricBreakOhmMin = dielectricBreakOhm

        this.electricalMaximalCurrent = electricalMaximalPower / electricalNominalVoltage

        if (this.electricalNominalVoltage > 4000.0) {
            voltageLevelColor = VoltageLevelColor.Grid
        } else {
            voltageLevelColor = VoltageLevelColor.fromCable(this)
        }
    }

    override fun applyTo(electricalLoad: ElectricalLoad, rsFactor: Double) {
        electricalLoad.serialResistance = electricalRs * rsFactor
    }

    override fun applyTo(electricalLoad: ElectricalLoad) {
        applyTo(electricalLoad, 1.0)
    }

    override fun applyTo(resistor: Resistor) {
        applyTo(resistor, 1.0)
    }

    override fun applyTo(resistor: Resistor, factor: Double) {
        resistor.resistance = electricalRs * factor
    }

    override fun applyTo(thermalLoad: ThermalLoad) {
        thermalLoad.Rs = thermalRs
        thermalLoad.heatCapacity = thermalC
        thermalLoad.Rp = thermalRp
    }
    
    override fun addRealismContext(list: MutableList<String>): RealisticEnum? {
        list.add(tr("Has some caveats:"))
        list.add(tr("  * Wire resistance is much higher than normal"))
        list.add(tr("  * Wire resistance is not impacted by temperature"))
        list.add(tr("  * Wire voltage limits are arbitrary values, picked to within reasonable simulator error"))
        list.add(tr("  * Wire current limits are arbitrary values, added as a gameplay mechanic"))
        return RealisticEnum.REALISTIC
    }
    
    override fun addInformation(itemStack: ItemStack, entityPlayer: Player?, list: MutableList<String>, par4: Boolean) {
        super.addInformation(itemStack, entityPlayer, list, par4)
        list.add(tr("Nominal Ratings:"))
        list.add("  " + tr("Voltage: %1\$V", Utils.plotValue(electricalNominalVoltage)))
        list.add("  " + tr("Current: %1\$A", Utils.plotValue(electricalNominalPower / electricalNominalVoltage)))
        list.add("  " + tr("Power: %1\$W", Utils.plotValue(electricalNominalPower)))
        list.add("  " + tr("Serial resistance: %1$\u2126", Utils.plotValue(electricalRs * 2)))
        
        if (description.isNotEmpty()) {
             Collections.addAll(list, *description.split("\n").toTypedArray())
        }
    }
}

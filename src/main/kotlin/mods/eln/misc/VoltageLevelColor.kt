package mods.eln.misc

import mods.eln.Eln
import mods.eln.sixnode.currentcable.CurrentCableDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import net.minecraft.resources.ResourceLocation
// import net.minecraftforge.client.IItemRenderer.ItemRenderType // Removed
import org.lwjgl.opengl.GL11

enum class VoltageLevelColor(private val voltageLevel: String?) {
    None(null),
    Neutral("neutral"),
    SignalVoltage("signal"),
    LowVoltage("low"),
    MediumVoltage("medium"),
    HighVoltage("high"),
    VeryHighVoltage("veryhigh"),
    Grid("grid"),
    Thermal("thermal");

    fun drawIconBackground() { // Removed ItemRenderType
        // Legacy rendering removed
    }

    fun getRed(): Float {
        return when (this) {
            SignalVoltage -> .80f
            LowVoltage -> .55f
            MediumVoltage -> .55f
            HighVoltage -> .96f
            VeryHighVoltage -> .96f
            Grid -> .8f
            Thermal -> 1f
            else -> 1f
        }
    }
    fun getGreen(): Float {
        return when (this) {
            SignalVoltage -> .87f
            LowVoltage -> .84f
            MediumVoltage -> .74f
            HighVoltage -> .80f
            VeryHighVoltage -> .56f
            Grid -> .8f
            Thermal -> 1f
            else -> 1f
        }
    }
    fun getBlue(): Float {
        return when (this) {
            SignalVoltage -> .82f
            LowVoltage -> .68f
            MediumVoltage -> .85f
            HighVoltage -> .56f
            VeryHighVoltage -> .56f
            Grid -> .8f
            Thermal -> 1f
            else -> 1f
        }
    }

    fun setGLColor() {
        GL11.glColor3f(getRed(), getGreen(), getBlue())
    }

    companion object {
        @JvmStatic
        fun fromVoltage(voltage: Double): VoltageLevelColor {
            return if (voltage < 0) {
                None
            } else if (voltage <= 2 * Eln.LVU) {
                LowVoltage
            } else if (voltage <= 2 * Eln.MVU) {
                MediumVoltage
            } else if (voltage <= 2 * Eln.HVU) {
                HighVoltage
            } else if (voltage <= 2 * Eln.VVU) {
                VeryHighVoltage
            } else if (voltage <= 2 * Eln.CCU) {
                Neutral
            } else {
                None
            }
        }

        @JvmStatic
        fun fromCable(descriptor: ElectricalCableDescriptor?): VoltageLevelColor {
            return if (descriptor != null) {
                if (descriptor.signalWire) {
                    SignalVoltage
                } else {
                    fromVoltage(descriptor.electricalNominalVoltage)
                }
            } else {
                None
            }
        }

        @JvmStatic
        fun fromCable(descriptor: CurrentCableDescriptor?): VoltageLevelColor {
            return if (descriptor != null) {
                Neutral
            } else {
                None
            }
        }
    }
}

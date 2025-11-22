package mods.eln.sixnode.electricalfiredetector

import mods.eln.item.electricalitem.BatteryItem
import mods.eln.misc.Coordinate
import mods.eln.misc.RcInterpolator
import mods.eln.sim.IProcess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.FireBlock
import net.minecraft.network.chat.Component
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.node.six.SixNodeElement

class ElectricalFireDetectorSlowProcess(val element: ElectricalFireDetectorElement) : IProcess {

    var rc: RcInterpolator? = null
    var t = 0.0

    init {
        if (!element.fireDescriptor.batteryPowered) {
            rc = RcInterpolator(0.6f)
        }
    }

    fun getBatteryLevel(): Double {
        val batteryStack = element.inventory!!.getItem(ElectricalFireDetectorContainer.BatteryId)
        val battery = GenericItemUsingDamageDescriptor.getDescriptor(batteryStack) as? BatteryItem
        return if (battery != null) {
            battery.getEnergy(batteryStack) / battery.getEnergyMax(batteryStack)
        } else {
            0.0
        }
    }

    override fun process(time: Double) {
        if (element.fireDescriptor.batteryPowered) {
            val batteryStack = element.inventory!!.getItem(ElectricalFireDetectorContainer.BatteryId)
            val battery = GenericItemUsingDamageDescriptor.getDescriptor(batteryStack) as? BatteryItem
            var energy = 0.0
            if (battery == null || battery.getEnergy(batteryStack).also { energy = it } < ElectricalFireDetectorDescriptor.PowerComsumption * time * 4) {
                val changed = element.powered
                element.powered = false
                if (changed) {
                    element.firePresent = false
                    element.needPublish()
                }
                return
            } else {
                val changed = !element.powered
                element.powered = true
                if (changed) element.needPublish()
                battery.setEnergy(batteryStack, energy - ElectricalFireDetectorDescriptor.PowerComsumption * time)
            }
        }

        t += time
        if (t >= element.fireDescriptor.updateInterval) {
            t = 0.0
            var fireDetected = false

            val maxRangeHalf = ((element.fireDescriptor.maxRange - 1) / 2).toInt()
            val detectionBBCenter = Coordinate((element as SixNodeElement).sixNode.coordinate!!.x, (element as SixNodeElement).sixNode.coordinate!!.y, (element as SixNodeElement).sixNode.coordinate!!.z, (element as SixNodeElement).sixNode.coordinate!!.dimension)
            when (element.side) {
                mods.eln.misc.Direction.XP -> detectionBBCenter.x -= maxRangeHalf
                mods.eln.misc.Direction.XN -> detectionBBCenter.x += maxRangeHalf
                mods.eln.misc.Direction.YP -> detectionBBCenter.y -= maxRangeHalf
                mods.eln.misc.Direction.YN -> detectionBBCenter.y += maxRangeHalf
                mods.eln.misc.Direction.ZP -> detectionBBCenter.z -= maxRangeHalf
                mods.eln.misc.Direction.ZN -> detectionBBCenter.z += maxRangeHalf
            }

            for (dx in -maxRangeHalf..maxRangeHalf) {
                for (dy in -maxRangeHalf..maxRangeHalf) {
                    for (dz in -maxRangeHalf..maxRangeHalf) {
                        val block = detectionBBCenter.getBlock((element as SixNodeElement).sixNode.level!!, dx, dy, dz)
                        if (block is FireBlock) {
                            fireDetected = true
                            break
                        }
                    }
                    if (fireDetected) break
                }
                if (fireDetected) break
            }

            if (element.fireDescriptor.batteryPowered) {
                if (fireDetected != element.firePresent) {
                    element.firePresent = fireDetected
                    element.needPublish()
                }
            } else {
                rc!!.target = if (fireDetected) 1.0f else 0.0f
                rc!!.step(element.fireDescriptor.updateInterval.toFloat())
                element.outputGate!!.setVoltage(rc!!.get().toDouble() * 50.0)
            }
        }
    }
}

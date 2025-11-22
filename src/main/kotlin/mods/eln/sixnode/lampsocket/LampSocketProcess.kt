package mods.eln.sixnode.lampsocket

import mods.eln.Eln
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.item.LampDescriptor
import mods.eln.misc.Coordinate
import mods.eln.misc.INBTTReady
import mods.eln.misc.Utils
import mods.eln.server.SaveConfig
import mods.eln.sim.IProcess
import mods.eln.transparentnode.LampSupplyElement
import mods.eln.transparentnode.LampSupplyElement.PowerSupplyChannelHandle
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

class LampSocketProcess(var lamp: LampSocketElement) : IProcess, INBTTReady /*,LightBlockObserver*/ {
    @JvmField
    var light = 0 // 0..15
    @JvmField
    var alphaZ = 0.0
    var stableProb = 0.0
    var lampStackLast: ItemStack? = null
    var boot = true
    var lbCoord: Coordinate

    var bestChannelHandle: Pair<Double, PowerSupplyChannelHandle>? = null

    private fun findBestSupply(here: Coordinate, forceUpdate: Boolean = false): Pair<Double, PowerSupplyChannelHandle>? {
        val chanMap = LampSupplyElement.channelMap[lamp.channel] ?: return null
        val bestChanHand = bestChannelHandle
        // Here's our cached value. We just check if it's null and if it's still a thing.
        if (bestChanHand != null && !forceUpdate && chanMap.contains(bestChanHand.second)) {
            return bestChanHand // we good!
        }
        val list = LampSupplyElement.channelMap[lamp.channel]?.filterNotNull() ?: return null
        val chanHand = list
            .map { Pair(it.element.sixNode?.coordinate?.trueDistanceTo(here)?: Double.MAX_VALUE, it) }
            .filter { it.first < it.second.element.range }
            .minByOrNull { it.first }
        bestChannelHandle = chanHand
        return bestChannelHandle
    }

    fun rotateAroundZ(v: Vec3, par1: Float): Vec3 {
        val f1 = Mth.cos(par1)
        val f2 = Mth.sin(par1)
        val d0 = v.x * f1.toDouble() + v.y * f2.toDouble()
        val d1 = v.y * f1.toDouble() - v.x * f2.toDouble()
        val d2 = v.z
        return Vec3(d0, d1, d2)
    }

    fun rotateAroundY(v: Vec3, par1: Float): Vec3 {
        val f1 = Mth.cos(par1)
        val f2 = Mth.sin(par1)
        val d0 = v.x * f1.toDouble() + v.z * f2.toDouble()
        val d1 = v.y
        val d2 = v.z * f1.toDouble() - v.x * f2.toDouble()
        return Vec3(d0, d1, d2)
    }

    private fun updateNearbyBlocks(growRate: Double, nominalLight: Double, actualLight: Int, deltaT: Double) {
        val randTarget = 1.0 / growRate * deltaT * (1.0 * actualLight / nominalLight / 15.0)
        if (randTarget > Math.random()) {
            var exit = false
            var vv = Vec3(1.0, 0.0, 0.0)
            var vp = Utils.getVec05(lamp.sixNode!!.coordinate!!)
            vv = rotateAroundZ(vv, (alphaZ * Math.PI / 180.0).toFloat())
            vv = rotateAroundY(vv, ((Math.random() - 0.5) * 2 * Math.PI / 4).toFloat())
            vv = rotateAroundZ(vv, ((Math.random() - 0.5) * 2 * Math.PI / 4).toFloat())
            
            vv = lamp.front!!.rotateOnXnLeft(vv)
            vv = lamp.side!!.rotateFromXN(vv)
            val c = Coordinate(lamp.sixNode!!.coordinate!!)
            for (idx in 0 until lamp.socketDescriptor.range + actualLight) { 
                vp = vp.add(vv)
                c.x = Mth.floor(vp.x)
                c.y = Mth.floor(vp.y)
                c.z = Mth.floor(vp.z)
                if (!lamp.sixNode!!.level!!.isLoaded(c.toBlockPos())) {
                    exit = true
                    break
                }
                if (isOpaque(c)) {
                    vp = vp.subtract(vv)
                    c.x = Mth.floor(vp.x)
                    c.y = Mth.floor(vp.y)
                    c.z = Mth.floor(vp.z)
                    break
                }
            }
            if (!exit && c.getBlock(lamp.sixNode!!.level!!) !== Blocks.AIR) {
                val world = lamp.sixNode!!.level!!
                if (world is net.minecraft.server.level.ServerLevel) {
                    c.getBlock(world).randomTick(world.getBlockState(net.minecraft.core.BlockPos(c.x, c.y, c.z)), world, net.minecraft.core.BlockPos(c.x, c.y, c.z), world.random)
                }
            }
        }
    }

    fun placeSpot(newLight: Int) {
        var exit = false
        if (!lamp.sixNode!!.level!!.isLoaded(lbCoord.toBlockPos())) return
        var vv = Vec3(1.0, 0.0, 0.0)
        var vp = Utils.getVec05(lamp.sixNode!!.coordinate!!)
        vv = rotateAroundZ(vv, (alphaZ * Math.PI / 180.0).toFloat())
        vv = lamp.front!!.rotateOnXnLeft(vv)
        vv = lamp.side!!.rotateFromXN(vv)
        val newCoord = Coordinate(lamp.sixNode!!.coordinate!!)
        for (idx in 0 until lamp.socketDescriptor.range) { 
            vp = vp.add(vv)
            newCoord.x = Mth.floor(vp.x)
            newCoord.y = Mth.floor(vp.y)
            newCoord.z = Mth.floor(vp.z)
            if (!lamp.sixNode!!.level!!.isLoaded(newCoord.toBlockPos())) {
                exit = true
                break
            }
            if (isOpaque(newCoord)) {
                vp = vp.subtract(vv)
                newCoord.x = Mth.floor(vp.x)
                newCoord.y = Mth.floor(vp.y)
                newCoord.z = Mth.floor(vp.z)
                break
            }
        }
        if (!exit) {
            var count = 0
            while (newCoord != lamp.sixNode!!.coordinate) {
                val block = newCoord.getBlock(lamp.sixNode!!.level!!)
                if (block === Blocks.AIR) {
                    count++
                    if (count == 2) break
                }
                vp = vp.subtract(vv)
                newCoord.x = Mth.floor(vp.x)
                newCoord.y = Mth.floor(vp.y)
                newCoord.z = Mth.floor(vp.z)
            }
        }
        if (!exit) setLightAt(newCoord, newLight)
    }

    private fun isOpaque(coord: Coordinate): Boolean {
        val world = lamp.sixNode?.level ?: return false
        val pos = net.minecraft.core.BlockPos(coord.x, coord.y, coord.z)
        val state = world.getBlockState(pos)
        return !state.isAir && (state.canOcclude() && state.block !== Blocks.FARMLAND)
    }

    fun setLightAt(coord: Coordinate, value: Int) {
        // TODO: Reimplement dynamic lighting
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        stableProb = nbt.getDouble(str + "LSP" + "stableProb")
        lbCoord.readFromNBT(nbt, str + "lbCoordInst")
        alphaZ = nbt.getFloat(str + "alphaZ").toDouble()
        light = nbt.getInt(str + "light")
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        nbt.putDouble(str + "LSP" + "stableProb", stableProb)
        lbCoord.writeToNBT(nbt, str + "lbCoordInst")
        nbt.putFloat(str + "alphaZ", alphaZ.toFloat())
        nbt.putInt(str + "light", light)
    }

    val blockLight: Int
        get() = if (lbCoord == lamp.sixNode!!.coordinate) {
            light
        } else {
            0
        }

    init {
        lbCoord = Coordinate(lamp.sixNode!!.coordinate!!)
    }

    override fun process(time: Double) {
        var newLight: Int
        val lampStack = lamp.inventory!!.getItem(LampSocketContainer.lampSlotId)
        val cableStack = lamp.inventory!!.getItem(LampSocketContainer.cableSlotId)
        // LampDescriptor? is *important* here. Otherwise, NPE.
        val lampDescriptor = GenericItemUsingDamageDescriptor.getDescriptor(lampStack) as? LampDescriptor
        if (cableStack.isEmpty || lampStack.isEmpty) {
            /*
            Cable slot and lamp slot are empty. This means no light, and disconnect from lamp supply.
             */
            stableProb = 0.0
            lamp.setIsConnectedToLampSupply(false)
        } else {
            /*
            Cable slot and lamp slot have stuff. This means there can be light.
             */
            if (lamp.poweredByLampSupply) {
                // Powered by a lamp supply.
                val lampSupplyList = findBestSupply(lamp.sixNode!!.coordinate!!)
                val bestLampSupply = lampSupplyList?.second
                if (bestLampSupply != null && lampDescriptor != null && bestLampSupply.element.getChannelState(bestLampSupply.id)) {
                    bestLampSupply.element.addToRp(lampDescriptor.r)
                    lamp.positiveLoad.state = bestLampSupply.element.powerLoad.state
                } else {
                    lamp.positiveLoad.state = 0.0
                }

                lamp.setIsConnectedToLampSupply(bestLampSupply != null)
            }
            else
            {
                // Not powered by a lamp supply.
                lamp.setIsConnectedToLampSupply(false)
            }
        }

        lamp.computeElectricalLoad()
        if (lampStack != lampStackLast) {
            stableProb = 0.0
        }

        if (stableProb < 0) stableProb = 0.0
        var lightDouble = 0.0

        if (lampDescriptor != null) {
            // This code makes the ECO lights blink, and the other lights are just "stable"
            when (lampDescriptor.type) {
                LampDescriptor.Type.INCANDESCENT, LampDescriptor.Type.LED -> {
                    if (lamp.lampResistor.voltage < lampDescriptor.minimalU) {
                        lightDouble = 0.0
                    } else {
                        lightDouble = lampDescriptor.nominalLight * ((Math.abs(lamp.lampResistor.voltage) - lampDescriptor.minimalU) / (lampDescriptor.nominalU - lampDescriptor.minimalU))
                    }
                    lightDouble *= 15
                }
                LampDescriptor.Type.ECO -> {
                    val U = Math.abs(lamp.lampResistor.voltage)
                    if (U < lampDescriptor.minimalU) {
                        stableProb = 0.0
                        lightDouble = 0.0
                    } else {
                        val powerFactor = U / lampDescriptor.nominalU
                        stableProb += U / lampDescriptor.stableU * time / lampDescriptor.stableTime * lampDescriptor.stableUNormalised
                        if (stableProb > U / lampDescriptor.stableU) stableProb = U / lampDescriptor.stableU
                        if (Math.random() > stableProb) {
                            lightDouble = 0.0
                        } else {
                            lightDouble = lampDescriptor.nominalLight * powerFactor
                            lightDouble *= 16
                        }
                    }
                }
            }
        } else {
            // there is no light bulb. Light = 0
            lightDouble = 0.0
        }

        newLight = lightDouble.toInt()
        //light.coerceIn(0, 15) // IT F'ING LIES
        if (newLight > 15) newLight = 15
        if (newLight < 0) newLight = 0

        fun lampAgeFactor(voltage: Double): Double {
            return 0.000008 * Math.pow(voltage, 3.0) - 0.003225 * Math.pow(voltage, 2.0) + 0.33 * voltage
        }

        if (lampDescriptor != null) {
            val bulbCanAge = !(lampDescriptor.type == LampDescriptor.Type.LED && Eln.ledLampInfiniteLife) && SaveConfig.instance!!.electricalLampAging

            if (bulbCanAge) {
                val ageFactor = lampAgeFactor(lamp.lampResistor.voltage)
                // life lost in hours, per tick
                val lifeLost = (ageFactor * time) / 3600.0
                lampDescriptor.setLifeInTag(lampStack, lampDescriptor.getLifeInTag(lampStack) - lifeLost)
            }
            if (lampDescriptor.getLifeInTag(lampStack) <= 0.0) {
                lamp.inventory!!.setItem(0, ItemStack.EMPTY)
                newLight = 0
            }
        } else {
            newLight = 0
        }

        if (lamp.sixNode!!.level!!.isLoaded(lamp.sixNode!!.coordinate!!.toBlockPos()) && lampDescriptor != null && lampDescriptor.vegetableGrowRate != 0.0) {
            updateNearbyBlocks(lampDescriptor.vegetableGrowRate, lampDescriptor.nominalLight, newLight, time)
        }

        boot = false
        lampStackLast = lampStack
        placeSpot(newLight)
        if (light != newLight)
            lamp.needPublish()
    }
}

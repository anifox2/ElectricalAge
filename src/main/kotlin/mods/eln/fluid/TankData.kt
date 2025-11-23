package mods.eln.fluid

import mods.eln.misc.INBTTReady
import mods.eln.misc.Utils
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.registries.ForgeRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fluids.capability.templates.FluidTank
import java.lang.Exception

data class TankData(val tank: FluidTank, val fluidWhitelist: MutableList<Fluid> = mutableListOf(), var fractionalDemandMb: Double = 0.0):
    INBTTReady {

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        tank.readFromNBT(nbt.getCompound("${str}tank"))
        val fluidWhitelistNames = nbt.getString("${str}whitelist")?.split("|")!!
        fluidWhitelist.clear()
        fluidWhitelistNames.forEach {
            try {
                if (it.isNotEmpty()) {
                    val fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation(it))
                    if (fluid != null) {
                        fluidWhitelist.add(fluid)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        val tankTag = CompoundTag()
        tank.writeToNBT(tankTag)
        nbt.put("${str}tank", tankTag)
        var fluidWhitelistNames = ""
        fluidWhitelist.forEach { fluidWhitelistNames += ForgeRegistries.FLUIDS.getKey(it).toString() + "|" }
        nbt.putString("${str}whitelist", fluidWhitelistNames)
    }
}

import os

def replace_in_file(filepath, old, new):
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        return
    with open(filepath, 'r') as f:
        content = f.read()
    if old in content:
        content = content.replace(old, new)
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Updated {filepath}")
    else:
        print(f"String not found in {filepath}: {old}")

# Fix Eln.kt
replace_in_file('src/main/kotlin/mods/eln/Eln.kt', 'const val maxSoundDistance = 64.0', 'var maxSoundDistance = 64.0')

# Fix GhostManagerNbt.kt
ghost_nbt_path = 'src/main/kotlin/mods/eln/ghost/GhostManagerNbt.kt'
with open(ghost_nbt_path, 'r') as f:
    content = f.read()

content = content.replace('import net.minecraft.world.level.LevelSavedData', 'import net.minecraft.world.level.saveddata.SavedData')
content = content.replace('class GhostManagerNbt(par1Str: String?) : WorldSavedData(par1Str)', 'class GhostManagerNbt : SavedData()')
content = content.replace('override fun load(nbt: CompoundTag)', 'fun load(nbt: CompoundTag)') # Remove override
content = content.replace('override fun isDirty(): Boolean', 'override fun isDirty(): Boolean') # This is fine if it exists, but SavedData uses setDirty() usually. 
# Actually isDirty() is boolean in SavedData? No, it has isDirty() method?
# In 1.20.1 SavedData has isDirty() -> boolean.
# But usually you call setDirty().
# Let's keep isDirty for now.

with open(ghost_nbt_path, 'w') as f:
    f.write(content)
print(f"Updated {ghost_nbt_path}")


# Fix TankData.kt
tank_data_path = 'src/main/kotlin/mods/eln/fluid/TankData.kt'
tank_data_content = """package mods.eln.fluid

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

    override fun load(nbt: CompoundTag, str: String) {
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

    override fun save(nbt: CompoundTag, str: String) {
        val tankTag = CompoundTag()
        tank.writeToNBT(tankTag)
        nbt.put("${str}tank", tankTag)
        var fluidWhitelistNames = ""
        fluidWhitelist.forEach { fluidWhitelistNames += ForgeRegistries.FLUIDS.getKey(it).toString() + "|" }
        nbt.putString("${str}whitelist", fluidWhitelistNames)
    }
}
"""
with open(tank_data_path, 'w') as f:
    f.write(tank_data_content)
print(f"Rewrote {tank_data_path}")

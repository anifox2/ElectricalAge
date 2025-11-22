package mods.eln.server

import mods.eln.Eln
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData

class SaveConfig : SavedData() {
    @JvmField
    var heatFurnaceFuel = true
    var electricalLampAging = true
    var batteryAging = true
    @JvmField
    var infinitePortableBattery = false
    var reGenOre = false
    var cableRsFactor_lastUsed = 1.0

    override fun save(nbt: CompoundTag): CompoundTag {
        nbt.putBoolean("heatFurnaceFuel", heatFurnaceFuel)
        nbt.putBoolean("electricalLampAging", electricalLampAging)
        nbt.putBoolean("batteryAging", batteryAging)
        nbt.putBoolean("infinitPortableBattery", infinitePortableBattery)
        nbt.putBoolean("reGenOre", reGenOre)
        Eln.wind.writeToNBT(nbt, "wind")
        return nbt
    }

    companion object {
        @JvmField
        var instance: SaveConfig? = null

        fun load(nbt: CompoundTag): SaveConfig {
            val config = SaveConfig()
            config.heatFurnaceFuel = nbt.getBoolean("heatFurnaceFuel")
            config.electricalLampAging = nbt.getBoolean("electricalLampAging")
            config.batteryAging = nbt.getBoolean("batteryAging")
            config.infinitePortableBattery = nbt.getBoolean("infinitPortableBattery")
            config.reGenOre = nbt.getBoolean("reGenOre")
            if (nbt.contains("cableRsFactor_lastUsed"))
                config.cableRsFactor_lastUsed = nbt.getDouble("cableRsFactor_lastUsed")
            Eln.wind.readFromNBT(nbt, "wind")
            instance = config
            return config
        }
    }
    
    init {
        instance = this
    }
}

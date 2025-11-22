package mods.eln.item

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player

interface IConfigurable {
    fun readConfigTool(compound: CompoundTag, invoker: Player)
    fun writeConfigTool(compound: CompoundTag, invoker: Player)
}

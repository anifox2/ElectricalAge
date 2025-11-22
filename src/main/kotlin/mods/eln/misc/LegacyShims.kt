package mods.eln.misc

import mods.eln.init.Registration
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.Tier
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity

object GameRegistry {
    fun registerItem(item: Item, name: String) {
        val cleanName = name.replace("Eln.", "").lowercase().replace(" ", "_")
        Registration.ITEMS.register(cleanName) { item }
    }

    fun registerBlock(block: Block, itemClass: Class<*>, name: String) {
        val cleanName = name.replace("Eln.", "").lowercase().replace(" ", "_")
        Registration.BLOCKS.register(cleanName) { block }
        // We should also register the item block here if possible, but for now just the block
    }

    fun registerCustomItemStack(name: String, stack: ItemStack) {
    }
}

object TileEntity {
    fun addMapping(clazz: Class<out BlockEntity>, name: String) {
        // In 1.20.1, BlockEntities are registered via DeferredRegister<BlockEntityType<?>>
        // This shim can't easily do that without more context.
        // For now, we'll ignore it or log it.
        println("Ignored TileEntity mapping for $name")
    }
}

fun Block.setCreativeTab(tab: Any?): Block = this
fun Block.setBlockName(name: String): Block = this

object OreDictionary {
    fun registerOre(name: String, item: Item) {}
    fun registerOre(name: String, stack: ItemStack) {}
    const val WILDCARD_VALUE = 32767
}

object EnumHelper {
    fun addArmorMaterial(name: String, durability: Int, reductionAmounts: IntArray, enchantability: Int): ArmorMaterial {
        return object : ArmorMaterial {
            override fun getDurabilityForType(type: ArmorItem.Type): Int {
                return durability
            }
            override fun getDefenseForType(type: ArmorItem.Type): Int {
                return when(type) {
                    ArmorItem.Type.HELMET -> reductionAmounts[0]
                    ArmorItem.Type.CHESTPLATE -> reductionAmounts[1]
                    ArmorItem.Type.LEGGINGS -> reductionAmounts[2]
                    ArmorItem.Type.BOOTS -> reductionAmounts[3]
                    else -> 0
                }
            }
            override fun getEnchantmentValue(): Int = enchantability
            override fun getEquipSound(): SoundEvent = SoundEvents.ARMOR_EQUIP_IRON
            override fun getRepairIngredient(): Ingredient = Ingredient.EMPTY
            override fun getName(): String = name.lowercase()
            override fun getToughness(): Float = 0f
            override fun getKnockbackResistance(): Float = 0f
        }
    }
}

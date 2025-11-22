package mods.eln.generic

import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.Item
import net.minecraft.world.item.CreativeModeTab

class genericArmorItem : ArmorItem {

    constructor(
        material: ArmorMaterial,
        renderIndex: Int,
        type: ArmourType,
        texture1: String,
        texture2: String
    ) : super(material, type.toMojangType(), Item.Properties())

    enum class ArmourType {
        Helmet, Chestplate, Leggings, Boots;
        
        fun toMojangType(): ArmorItem.Type {
            return when(this) {
                Helmet -> ArmorItem.Type.HELMET
                Chestplate -> ArmorItem.Type.CHESTPLATE
                Leggings -> ArmorItem.Type.LEGGINGS
                Boots -> ArmorItem.Type.BOOTS
            }
        }
    }
    
    fun setUnlocalizedName(name: String): genericArmorItem {
        return this
    }
    
    fun setTextureName(name: String): genericArmorItem {
        return this
    }
    
    fun setCreativeTab(tab: CreativeModeTab?): genericArmorItem {
        return this
    }
}

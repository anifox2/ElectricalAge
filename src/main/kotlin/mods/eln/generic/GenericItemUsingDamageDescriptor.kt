package mods.eln.generic

import mods.eln.init.Registration
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraftforge.registries.RegistryObject

open class GenericItemUsingDamageDescriptor(val name: String, var iconName: String? = null) {
    val registryObject: RegistryObject<Item>

    fun setDefaultIcon(iconName: String) {
        this.iconName = iconName
    }

    fun newItemStack(amount: Int = 1): ItemStack {
        return ItemStack(registryObject.get(), amount)
    }

    companion object {
        const val INVALID_NAME = "INVALID"
        private val descriptorMap = mutableMapOf<Item, GenericItemUsingDamageDescriptor>()
        private val nameMap = mutableMapOf<String, GenericItemUsingDamageDescriptor>()

        fun getDescriptor(stack: ItemStack): GenericItemUsingDamageDescriptor? {
            if (stack.isEmpty) return null
            return descriptorMap[stack.item]
        }

        fun getByName(name: String): GenericItemUsingDamageDescriptor? {
            return nameMap[name]
        }
    }

    init {
        // Sanitize name for registry
        val regName = name.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
        registryObject = Registration.ITEMS.register(regName) {
            val item = createItem()
            descriptorMap[item] = this
            item
        }
        nameMap[name] = this
    }

    open fun createItem(): Item {
        return object : Item(Item.Properties()) {
            override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
                this@GenericItemUsingDamageDescriptor.appendHoverText(stack, level, tooltip, flag)
            }
            override fun onEntityItemUpdate(stack: ItemStack, entity: ItemEntity): Boolean {
                return this@GenericItemUsingDamageDescriptor.onEntityItemUpdate(stack, entity)
            }
        }
    }

    open fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
    }

    open fun onEntityItemUpdate(stack: ItemStack, entity: ItemEntity): Boolean {
        return false
    }

    fun getStack(count: Int = 1): ItemStack {
        return ItemStack(registryObject.get(), count)
    }

    fun checkSameItemStack(stack: ItemStack): Boolean {
        return !stack.isEmpty && stack.item == registryObject.get()
    }
}

companion object {
        fun <T : GenericItemUsingDamageDescriptor> getDescriptor(stack: ItemStack, clazz: Class<T>): T? {
            return null // Stub
        }

        fun getDescriptor(stack: ItemStack): GenericItemUsingDamageDescriptor? {
            return null // Stub
        }
    }

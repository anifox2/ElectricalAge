package mods.eln.generic

import mods.eln.init.Registration
import mods.eln.misc.RealisticEnum
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraftforge.registries.RegistryObject

open class GenericItemUsingDamageDescriptor @JvmOverloads constructor(val name: String, var iconName: String? = null) {
    val registryObject: RegistryObject<Item>
    var parentItemDamage: Int = 0
    open fun setParent(registry: Any?, id: Int) {
        this.parentItemDamage = id
    }

    open fun getName(stack: ItemStack): Component {
        return Component.literal(name)
    }

    fun setDefaultIcon(iconName: String) {
        this.iconName = iconName
    }

    @JvmOverloads
    open fun newItemStack(amount: Int = 1): ItemStack {
        if (registryObject.isPresent) {
            return ItemStack(registryObject.get(), amount)
        }
        return ItemStack.EMPTY
    }

    companion object {
        const val INVALID_NAME = "INVALID"
        private val descriptorMap = mutableMapOf<Item, GenericItemUsingDamageDescriptor>()
        private val nameMap = mutableMapOf<String, GenericItemUsingDamageDescriptor>()

        @JvmStatic
        fun getDescriptor(stack: ItemStack): GenericItemUsingDamageDescriptor? {
            if (stack.isEmpty) return null
            return descriptorMap[stack.item]
        }

        @JvmStatic
        fun <T : GenericItemUsingDamageDescriptor> getDescriptor(stack: ItemStack, clazz: Class<T>): T? {
            val desc = getDescriptor(stack)
            return if (clazz.isInstance(desc)) clazz.cast(desc) else null
        }

        @JvmStatic
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
            override fun inventoryTick(stack: ItemStack, level: Level, entity: net.minecraft.world.entity.Entity, slotId: Int, isSelected: Boolean) {
                this@GenericItemUsingDamageDescriptor.inventoryTick(stack, level, entity, slotId, isSelected)
            }
            override fun use(level: Level, player: net.minecraft.world.entity.player.Player, hand: net.minecraft.world.InteractionHand): net.minecraft.world.InteractionResultHolder<ItemStack> {
                return this@GenericItemUsingDamageDescriptor.use(level, player, hand)
            }
            override fun onDroppedByPlayer(item: ItemStack, player: net.minecraft.world.entity.player.Player): Boolean {
                return this@GenericItemUsingDamageDescriptor.onDroppedByPlayer(item, player)
            }
            override fun onBlockStartBreak(itemstack: ItemStack, pos: net.minecraft.core.BlockPos, player: net.minecraft.world.entity.player.Player): Boolean {
                return this@GenericItemUsingDamageDescriptor.onBlockStartBreak(itemstack, pos, player)
            }
            override fun onEntitySwing(stack: ItemStack, entity: net.minecraft.world.entity.LivingEntity): Boolean {
                return this@GenericItemUsingDamageDescriptor.onEntitySwing(stack, entity)
            }
            override fun mineBlock(stack: ItemStack, level: Level, state: net.minecraft.world.level.block.state.BlockState, pos: net.minecraft.core.BlockPos, entity: net.minecraft.world.entity.LivingEntity): Boolean {
                return this@GenericItemUsingDamageDescriptor.mineBlock(stack, level, state, pos, entity)
            }
            override fun getDestroySpeed(stack: ItemStack, state: net.minecraft.world.level.block.state.BlockState): Float {
                return this@GenericItemUsingDamageDescriptor.getDestroySpeed(stack, state)
            }
            override fun getName(stack: ItemStack): Component {
                return this@GenericItemUsingDamageDescriptor.getName(stack)
            }
        }
    }

    open fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        val stringList = mutableListOf<String>()
        addInformation(stack, null, stringList, flag.isAdvanced)
        for (s in stringList) {
            tooltip.add(Component.literal(s))
        }
    }
    
    // Legacy shim
    open fun addInformation(stack: ItemStack, player: net.minecraft.world.entity.player.Player?, tooltip: MutableList<String>, advanced: Boolean) {}

    open fun onEntityItemUpdate(stack: ItemStack, entity: ItemEntity): Boolean = false
    open fun inventoryTick(stack: ItemStack, level: Level, entity: net.minecraft.world.entity.Entity, slotId: Int, isSelected: Boolean) {}
    open fun use(level: Level, player: net.minecraft.world.entity.player.Player, hand: net.minecraft.world.InteractionHand): net.minecraft.world.InteractionResultHolder<ItemStack> {
        return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand))
    }
    open fun onDroppedByPlayer(item: ItemStack, player: net.minecraft.world.entity.player.Player): Boolean = true
    open fun onBlockStartBreak(itemstack: ItemStack, pos: net.minecraft.core.BlockPos, player: net.minecraft.world.entity.player.Player): Boolean = false
    open fun onEntitySwing(stack: ItemStack, entity: net.minecraft.world.entity.LivingEntity): Boolean = false
    open fun mineBlock(stack: ItemStack, level: Level, state: net.minecraft.world.level.block.state.BlockState, pos: net.minecraft.core.BlockPos, entity: net.minecraft.world.entity.LivingEntity): Boolean = false
    open fun getDestroySpeed(stack: ItemStack, state: net.minecraft.world.level.block.state.BlockState): Float = 1.0f

    open fun getDefaultNBT(): net.minecraft.nbt.CompoundTag? = null

    fun getStack(count: Int = 1): ItemStack {
        return ItemStack(registryObject.get(), count)
    }

    fun checkSameItemStack(stack: ItemStack): Boolean {
        return !stack.isEmpty && stack.item == registryObject.get()
    }

    open fun addRealismContext(list: MutableList<String>): RealisticEnum {
        return RealisticEnum.IDEAL
    }
}

fun ItemStack.ensureTag(): net.minecraft.nbt.CompoundTag = this.orCreateTag

fun ItemStack.getIntTag(key: String): Int = this.tag?.getInt(key) ?: 0
fun ItemStack.setIntTag(key: String, value: Int) { ensureTag().putInt(key, value) }
fun ItemStack.getDoubleTag(key: String): Double = this.tag?.getDouble(key) ?: 0.0
fun ItemStack.putDoubleTag(key: String, value: Double) { ensureTag().putDouble(key, value) }
fun ItemStack.getFloatTag(key: String): Float = this.tag?.getFloat(key) ?: 0.0f
fun ItemStack.putFloatTag(key: String, value: Float) { ensureTag().putFloat(key, value) }
fun ItemStack.getBooleanTag(key: String): Boolean = this.tag?.getBoolean(key) ?: false
fun ItemStack.putBooleanTag(key: String, value: Boolean) { ensureTag().putBoolean(key, value) }

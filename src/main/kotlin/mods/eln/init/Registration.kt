package mods.eln.init

import mods.eln.Eln
import mods.eln.misc.elnMetadata
import mods.eln.node.transparent.TransparentNodeBlock
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.TransparentNodeItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.network.chat.Component
import net.minecraft.core.registries.Registries
import mods.eln.generic.GenericItemBlockUsingDamage
import mods.eln.generic.GenericItemUsingDamage
import mods.eln.ghost.GhostBlock
import mods.eln.simplenode.DeviceProbeBlock
import mods.eln.simplenode.DeviceProbeEntity
import mods.eln.node.simple.SimpleNodeItem
import mods.eln.simplenode.energyconverter.EnergyConverterElnToOtherBlock
import mods.eln.simplenode.energyconverter.EnergyConverterElnToOtherDescriptor
import mods.eln.simplenode.energyconverter.EnergyConverterElnToOtherEntity
import mods.eln.node.six.SixNodeBlock
import mods.eln.node.six.SixNodeItem
import mods.eln.node.six.SixNodeEntity
import mods.eln.sixnode.treeresincollector.TreeResinCollectorBlock
import mods.eln.sixnode.treeresincollector.TreeResinCollectorTileEntity
import net.minecraft.world.inventory.MenuType
import net.minecraftforge.common.extensions.IForgeMenuType
import mods.eln.transparentnode.FuelHeatFurnaceContainer
import mods.eln.transparentnode.FuelHeatFurnaceGui

object Registration {
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, Eln.MODID)
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, Eln.MODID)
    val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Eln.MODID)
    val MENU_TYPES: DeferredRegister<MenuType<*>> = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Eln.MODID)
    val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Eln.MODID)

    val FUEL_HEAT_FURNACE_MENU: RegistryObject<MenuType<FuelHeatFurnaceContainer>> = MENU_TYPES.register("fuel_heat_furnace") {
        IForgeMenuType.create(FuelHeatFurnaceContainer::create)
    }

    val HEAT_FURNACE_MENU: RegistryObject<MenuType<mods.eln.transparentnode.heatfurnace.HeatFurnaceContainer>> = MENU_TYPES.register("heat_furnace") {
        IForgeMenuType.create(mods.eln.transparentnode.heatfurnace.HeatFurnaceContainer::create)
    }

    val TRANSPARENT_NODE_BLOCK: RegistryObject<TransparentNodeBlock> = BLOCKS.register("transparent_node") { TransparentNodeBlock(BlockBehaviour.Properties.of().noOcclusion()) }
    val TRANSPARENT_NODE_ITEM: RegistryObject<Item> = ITEMS.register("transparent_node") { TransparentNodeItem(TRANSPARENT_NODE_BLOCK.get()) }
    val TRANSPARENT_NODE_BLOCK_ENTITY: RegistryObject<BlockEntityType<TransparentNodeBlockEntity>> = BLOCK_ENTITIES.register("transparent_node") {
        BlockEntityType.Builder.of(::TransparentNodeBlockEntity, TRANSPARENT_NODE_BLOCK.get()).build(null)
    }

    val DEVICE_PROBE_BLOCK: RegistryObject<DeviceProbeBlock> = BLOCKS.register("device_probe") { DeviceProbeBlock() }
    val DEVICE_PROBE_ITEM: RegistryObject<Item> = ITEMS.register("device_probe") { SimpleNodeItem(DEVICE_PROBE_BLOCK.get()) }
    val DEVICE_PROBE_BLOCK_ENTITY: RegistryObject<BlockEntityType<DeviceProbeEntity>> = BLOCK_ENTITIES.register("device_probe") {
        BlockEntityType.Builder.of(::DeviceProbeEntity, DEVICE_PROBE_BLOCK.get()).build(null)
    }

    val ENERGY_CONVERTER_BLOCK: RegistryObject<EnergyConverterElnToOtherBlock> = BLOCKS.register("energy_converter") { 
        EnergyConverterElnToOtherBlock(EnergyConverterElnToOtherDescriptor("EnergyConverter", 10000.0)) 
    }
    val ENERGY_CONVERTER_ITEM: RegistryObject<Item> = ITEMS.register("energy_converter") { SimpleNodeItem(ENERGY_CONVERTER_BLOCK.get()) }
    val ENERGY_CONVERTER_BLOCK_ENTITY: RegistryObject<BlockEntityType<EnergyConverterElnToOtherEntity>> = BLOCK_ENTITIES.register("energy_converter") {
        BlockEntityType.Builder.of(::EnergyConverterElnToOtherEntity, ENERGY_CONVERTER_BLOCK.get()).build(null)
    }

    val SIX_NODE_BLOCK: RegistryObject<SixNodeBlock> = BLOCKS.register("six_node") { SixNodeBlock(BlockBehaviour.Properties.of().noOcclusion()) }
    val SIX_NODE_ITEM: RegistryObject<SixNodeItem> = ITEMS.register("six_node") { SixNodeItem(SIX_NODE_BLOCK.get()) }
    val SIX_NODE_BLOCK_ENTITY: RegistryObject<BlockEntityType<SixNodeEntity>> = BLOCK_ENTITIES.register("six_node") {
        BlockEntityType.Builder.of(::SixNodeEntity, SIX_NODE_BLOCK.get()).build(null)
    }

    val GHOST_BLOCK: RegistryObject<GhostBlock> = BLOCKS.register("ghost_block") { GhostBlock(BlockBehaviour.Properties.of().noOcclusion()) }

    val TREE_RESIN_COLLECTOR_BLOCK: RegistryObject<TreeResinCollectorBlock> = BLOCKS.register("tree_resin_collector") { TreeResinCollectorBlock() }
    val TREE_RESIN_COLLECTOR_ITEM: RegistryObject<Item> = ITEMS.register("tree_resin_collector") { SimpleNodeItem(TREE_RESIN_COLLECTOR_BLOCK.get()) }
    val TREE_RESIN_COLLECTOR_BLOCK_ENTITY: RegistryObject<BlockEntityType<TreeResinCollectorTileEntity>> = BLOCK_ENTITIES.register("tree_resin_collector") {
        BlockEntityType.Builder.of(::TreeResinCollectorTileEntity, TREE_RESIN_COLLECTOR_BLOCK.get()).build(null)
    }

    val ELN_TAB: RegistryObject<CreativeModeTab> = CREATIVE_MODE_TABS.register("eln_tab") {
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.eln"))
            .icon { ItemStack(SIX_NODE_ITEM.get()) }
            .displayItems { _, output ->
                ITEMS.entries.forEach { regObj ->
                    val item = regObj.get()
                    if (item is GenericItemBlockUsingDamage<*>) {
                        for (id in item.orderList) {
                            val stack = ItemStack(item)
                            stack.elnMetadata = id
                            output.accept(stack)
                        }
                    } else if (item is GenericItemUsingDamage<*>) {
                        for (id in item.orderList) {
                            val stack = ItemStack(item)
                            stack.elnMetadata = id
                            output.accept(stack)
                        }
                    } else {
                        output.accept(item)
                    }
                }
            }
            .build()
    }

    @JvmStatic
    fun init(eventBus: IEventBus) {
        BLOCKS.register(eventBus)
        ITEMS.register(eventBus)
        BLOCK_ENTITIES.register(eventBus)
        MENU_TYPES.register(eventBus)
        CREATIVE_MODE_TABS.register(eventBus)
    }
}

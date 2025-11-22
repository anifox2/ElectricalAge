package mods.eln.init

import mods.eln.Eln
import mods.eln.node.transparent.TransparentNodeBlock
import mods.eln.node.transparent.TransparentNodeEntity
import mods.eln.node.transparent.TransparentNodeItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object Registration {
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, Eln.MODID)
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, Eln.MODID)
    val BLOCK_ENTITIES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Eln.MODID)

    val TRANSPARENT_NODE_BLOCK: RegistryObject<TransparentNodeBlock> = BLOCKS.register("transparent_node") { TransparentNodeBlock(BlockBehaviour.Properties.of().noOcclusion()) }
    val TRANSPARENT_NODE_ITEM: RegistryObject<Item> = ITEMS.register("transparent_node") { TransparentNodeItem(TRANSPARENT_NODE_BLOCK.get()) }
    val TRANSPARENT_NODE_BLOCK_ENTITY: RegistryObject<BlockEntityType<TransparentNodeEntity>> = BLOCK_ENTITIES.register("transparent_node") {
        BlockEntityType.Builder.of({ pos, state -> TransparentNodeEntity(TRANSPARENT_NODE_BLOCK_ENTITY.get(), pos, state) }, TRANSPARENT_NODE_BLOCK.get()).build(null)
    }

    fun init(eventBus: IEventBus) {
        BLOCKS.register(eventBus)
        ITEMS.register(eventBus)
        BLOCK_ENTITIES.register(eventBus)
    }
}

package mods.eln.fluid

import mods.eln.Eln
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.FlowingFluid
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions
import net.minecraftforge.common.SoundActions
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fluids.ForgeFlowingFluid
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import java.util.function.Consumer

object ElnFluids {
    val FLUID_TYPES: DeferredRegister<FluidType> = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "eln")
    val FLUIDS: DeferredRegister<Fluid> = DeferredRegister.create(ForgeRegistries.FLUIDS, "eln")
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, "eln")
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, "eln")

    val ENTRIES = mutableMapOf<ElnFluidRegistry, FluidEntry>()

    fun init(eventBus: IEventBus) {
        ElnFluidRegistry.values().forEach { fluidEnum ->
            ENTRIES[fluidEnum] = registerFluid(fluidEnum)
        }
        
        FLUID_TYPES.register(eventBus)
        FLUIDS.register(eventBus)
        BLOCKS.register(eventBus)
        ITEMS.register(eventBus)
    }

    data class FluidEntry(
        val type: RegistryObject<FluidType>,
        val source: RegistryObject<FlowingFluid>,
        val flowing: RegistryObject<FlowingFluid>,
        val block: RegistryObject<LiquidBlock>,
        val bucket: RegistryObject<BucketItem>
    )

    private fun registerFluid(fluidEnum: ElnFluidRegistry): FluidEntry {
        val fluidName = fluidEnum.name
        
        val typeProperties = FluidType.Properties.create()
            .density(fluidEnum.density)
            .viscosity(fluidEnum.viscosity)
            .temperature(fluidEnum.temperature)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)

        val type = FLUID_TYPES.register(fluidName) {
            object : FluidType(typeProperties) {
                override fun initializeClient(consumer: Consumer<IClientFluidTypeExtensions>) {
                    consumer.accept(object : IClientFluidTypeExtensions {
                        override fun getStillTexture(): ResourceLocation = ResourceLocation("eln", "block/fluids/${fluidName}_still")
                        override fun getFlowingTexture(): ResourceLocation = ResourceLocation("eln", "block/fluids/${fluidName}_flow")
                        override fun getTintColor(): Int = fluidEnum.color
                    })
                }
            }
        } as RegistryObject<FluidType>

        // We need to use lateinit or a wrapper because of circular references
        // But we can't use lateinit inside the function easily for the properties.
        // We can use RegistryObject.get() which is lazy.
        
        // We need to declare the RegistryObjects first.
        // But DeferredRegister.register returns the RegistryObject.
        
        // Workaround: Use a holder class or just rely on the fact that properties are evaluated lazily by ForgeFlowingFluid?
        // ForgeFlowingFluid.Properties takes Suppliers.
        
        // We need to define the variables first.
        var source: RegistryObject<FlowingFluid>? = null
        var flowing: RegistryObject<FlowingFluid>? = null
        var block: RegistryObject<LiquidBlock>? = null
        var bucket: RegistryObject<BucketItem>? = null

        val properties = ForgeFlowingFluid.Properties(
            { type.get() },
            { source!!.get() },
            { flowing!!.get() }
        ).block { block!!.get() }.bucket { bucket!!.get() }

        source = FLUIDS.register(fluidName) { ForgeFlowingFluid.Source(properties) }
        flowing = FLUIDS.register("${fluidName}_flowing") { ForgeFlowingFluid.Flowing(properties) }
        
        block = BLOCKS.register(fluidName) {
            BlockElnFluid({ source!!.get() }, BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable().noCollission().strength(100.0f).pushReaction(PushReaction.DESTROY).noLootTable().liquid())
        }
        
        bucket = ITEMS.register("${fluidName}_bucket") {
            BucketItem({ source!!.get() }, Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
        }

        return FluidEntry(type, source!!, flowing!!, block!!, bucket!!)
    }
}

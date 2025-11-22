package mods.eln

import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.init.Registration
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import mods.eln.cable.CableRenderDescriptor
import mods.eln.node.transparent.TransparentNodeItem
import mods.eln.client.ClientProxy
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.ghost.GhostBlock
import mods.eln.transparentnode.electricalfurnace.ElectricalFurnaceDescriptor
import mods.eln.sim.ThermalLoadInitializer
import java.util.ArrayList
import mods.eln.sixnode.currentcable.CurrentCableDescriptor
import mods.eln.sixnode.SixNodeItem
import mods.eln.sixnode.SixNodeDescriptor
import mods.eln.generic.genericArmorItem
import mods.eln.item.ItemPickaxeEln
import mods.eln.item.ItemAxeEln
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.ShovelItem
import net.minecraft.world.item.HoeItem

@Mod(Eln.MODID)
class Eln {
    init {
        val eventBus = FMLJavaModLoadingContext.get().getModEventBus()
        Registration.init(eventBus)
        eventBus.addListener(this::commonSetup)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        transparentNodeBlock = Registration.TRANSPARENT_NODE_BLOCK.get()
        transparentNodeBlockEntity = Registration.TRANSPARENT_NODE_BLOCK_ENTITY.get()
    }

    class SignalCableDescriptorStub {
        var render: CableRenderDescriptor? = null
        fun applyTo(o: Any?) {}
    }

    var electricalFrequency = 20.0
    var thermalFrequency = 20.0
    var testItem: GenericItemUsingDamageDescriptor? = null
    var signalCableDescriptor: mods.eln.sixnode.electricalcable.ElectricalCableDescriptor? = null
    var stdCableRenderSignal: CableRenderDescriptor? = null
    var lowCurrentCableRender: CableRenderDescriptor? = null
    var featureMap = java.util.HashMap<String, String>()
    var forceOreRegen = false
    var oreBlock: net.minecraft.world.level.block.Block? = null
    var oreItem: GenericItemUsingDamageDescriptor? = null
    
    var batteryCapacityFactor = 1.0
    var stdBatteryHalfLife = 3600.0 * 24 * 365

    var lowVoltageCableDescriptor: ElectricalCableDescriptor? = null
    var meduimVoltageCableDescriptor: ElectricalCableDescriptor? = null
    var highVoltageCableDescriptor: ElectricalCableDescriptor? = null
    var veryHighVoltageCableDescriptor: ElectricalCableDescriptor? = null
    var compressorRecipes = mods.eln.misc.RecipesList()
    var solarPanelPowerFactor = 1.0
    var windTurbinePowerFactor = 1.0
    var waterTurbinePowerFactor = 1.0
    var fuelGeneratorPowerFactor = 1.0
    var fuelGeneratorTankCapacity = 1000.0
    var heatTurbinePowerFactor = 1.0
    var sixNodeThermalLoadInitializer = ThermalLoadInitializer()
    var furnaceList = ArrayList<ItemStack>()
    var electricalFurnace: ElectricalFurnaceDescriptor? = null
    var magnetiserRecipes = mods.eln.misc.RecipesList()
    var batteryVoltageFunctionTable: mods.eln.misc.FunctionTable? = null

    var stdCableRenderSignalBus: CableRenderDescriptor? = null
    var stdCableRender50V: CableRenderDescriptor? = null
    var stdCableRender200V: CableRenderDescriptor? = null
    var stdCableRender800V: CableRenderDescriptor? = null
    var stdCableRender3200V: CableRenderDescriptor? = null
    var stdCableRenderCreative: CableRenderDescriptor? = null

    var signalBusCableDescriptor: ElectricalCableDescriptor? = null
    var creativeCableDescriptor: ElectricalCableDescriptor? = null

    var lowCurrentCableDescriptor: CurrentCableDescriptor? = null
    var mediumCurrentCableDescriptor: CurrentCableDescriptor? = null
    var highCurrentCableDescriptor: CurrentCableDescriptor? = null

    var mediumCurrentCableRender: CableRenderDescriptor? = null
    var highCurrentCableRender: CableRenderDescriptor? = null

    var stdPortableNaN: CableRenderDescriptor? = null
    var portableNaNDescriptor: mods.eln.sixnode.PortableNaNDescriptor? = null

    var isDevelopmentRun = false

    var ElnToOtherEnergyConverterEnable = true
    var ELN_CONVERTER_MAX_POWER = 10000.0
    var elnToOtherBlockConverter: net.minecraft.world.level.block.Block? = null
    var ComputerProbeEnable = true
    var computerProbeBlock: net.minecraft.world.level.block.Block? = null

    fun LVP() = 200.0
    fun MVP() = 800.0
    fun HVP() = 3200.0
    fun VVP() = 12800.0

    init {
        instance = this
        val modEventBus = FMLJavaModLoadingContext.get().getModEventBus()
        modEventBus.addListener { event: FMLCommonSetupEvent -> this.commonSetup(event) }
        modEventBus.addListener { event: FMLClientSetupEvent -> this.clientSetup(event) }

        Registration.init(modEventBus)

        testItem = GenericItemUsingDamageDescriptor("Test Item")
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("Electrical Age Common Setup")
    }

    private fun clientSetup(event: FMLClientSetupEvent) {
        LOGGER.info("Electrical Age Client Setup")
        ClientProxy.registerRenderers()
    }

    companion object {
        lateinit var instance: Eln
        const val MODID = "eln"
        const val LVU = 50.0
        const val MVU = 200.0
        const val HVU = 800.0
        const val SVU = 200.0
        const val SVP = 800.0 // Guessing 4x SVU
        const val SVII = 1.0 // Guessing
        const val VVU = 3200.0 // Guessing
        const val cableThermalConductionTao = 1.0 // Guessing
        const val wirelessTxRange = 64.0

        const val gateOutputCurrent = 0.100
        const val CCU = 50.0
        const val cableWarmLimit = 100.0
        const val cableHeatingTime = 10.0
        const val packetPlaySound = 1
        const val packetDestroyUuid: Byte = 2
        const val maxSoundDistance = 64.0

        lateinit var sixNodeItem: SixNodeItem
        var sharedItem = mods.eln.generic.GenericItemRegistry()
        var cableThermalLoadInitializer = ThermalLoadInitializer()

        fun getSmallRs() = 0.001

        var transistor: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var alu: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var siliconWafer: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var plateCopper: mods.eln.generic.GenericItemUsingDamageDescriptor? = null

        lateinit var transparentNodeItem: TransparentNodeItem
        var solarPanelBasePower = 40.0
        lateinit var ghostBlock: GhostBlock

        var batteryVoltageFunctionTable = mods.eln.misc.FunctionTable(doubleArrayOf(0.0, 1.0), 1.0)

        val obj = mods.eln.misc.Obj3DFolder()
        
        @JvmField
        var simulator: mods.eln.sim.Simulator = mods.eln.sim.Simulator()
        var wailaEasyMode = false
        var debugEnabled = false
        var explosionEnable = true
        var noSymbols = false
        var debugExplosions = false

        fun findItemStack(name: String, amount: Int): ItemStack {
            return ItemStack(net.minecraft.world.item.Items.AIR)
        }

        var ledLampInfiniteLife = false
        var wind = mods.eln.misc.WindProcess()

        var modbusEnable = true
        var enableFestivities = true

        fun applySmallRs(load: mods.eln.sim.ElectricalLoad) {
            load.setRs(getSmallRs())
        }

        var veryHighVoltageCableDescriptor: mods.eln.sixnode.electricalcable.ElectricalCableDescriptor? = null
        var lowVoltageCableDescriptor: mods.eln.sixnode.electricalcable.ElectricalCableDescriptor? = null
        var copperCableDescriptor: mods.eln.cable.CopperCableDescriptor? = null
        var ghostManager: mods.eln.ghost.GhostManager? = null
        
        var helmetCopper: genericArmorItem? = null
        var chestplateCopper: genericArmorItem? = null
        var legsCopper: genericArmorItem? = null
        var bootsCopper: genericArmorItem? = null
        
        var helmetECoal: genericArmorItem? = null
        var plateECoal: genericArmorItem? = null
        var legsECoal: genericArmorItem? = null
        var bootsECoal: genericArmorItem? = null
        
        var pickaxeCopper: ItemPickaxeEln? = null
        var axeCopper: ItemAxeEln? = null
        var shovelCopper: ShovelItem? = null
        var swordCopper: SwordItem? = null
        var hoeCopper: HoeItem? = null
        
        var creativeTab: net.minecraft.world.item.CreativeModeTab? = null
        
        var oreCopper: mods.eln.ore.OreDescriptor? = null
        
        var oredictTungsten = false
        var oredictChips = true
        
        var oreNames = ArrayList<String>()
        
        var oreScannerConfig = ArrayList<mods.eln.item.electricalitem.PortableOreScannerItem.OreScannerConfigElement>()
        
        var oreItem: mods.eln.item.OreItem? = null
        var oreBlock: net.minecraft.world.level.block.Block? = null

        var miningPipeDescriptor: mods.eln.item.MiningPipeDescriptor? = null
        
        var sharedItemStackOne = sharedItem
        
        var incandescentLampLife = 0.0
        var carbonLampLife = 0.0
        var economicLampLife = 0.0
        var ledLampLife = 0.0
        
        var dictionnaryOreFromMod = java.util.HashMap<String, net.minecraft.world.item.ItemStack>()
        
        var dustCopper: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var copperIngot: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var plumbIngot: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var tungstenIngot: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        
        var dictSiliconWafer = true
        var dictTransistor = true
        var dictThermistor = true
        var dictNibbleMemory = true
        var dictALU = true
        var dictTungstenOre = true
        var dictTungstenDust = true
        var dictTungstenIngot = true
        var dictCheapChip = true
        var dictAdvancedChip = true
        
        var thermistor: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var nibbleMemory: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        
        var multiMeterElement: Any? = null
        var thermometerElement: Any? = null
        var allMeterElement: Any? = null
        var configCopyToolElement: Any? = null
        
        var treeResin: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        
        var xRayScannerRange = 0.0
        var fuelHeatFurnacePowerFactor = 1.0
        
        var dataLogsPrintDescriptor: Any? = null
        
        var wrenchItemStack: net.minecraft.world.item.ItemStack? = null
        
        var whiteDesc: Any? = null
        var brushSubNames: Array<String>? = null
        
        var genCopper: Boolean = true
        var genLead: Boolean = true
        var genTungsten: Boolean = true
        var genCinnabar: Boolean = true

        val AUTHORS = listOf("Baughn", "Briman", "Dries007", "Maeyanie", "Mr_Hazard", "Xbony2")

        val saveConfig: mods.eln.server.SaveConfig
            get() = mods.eln.server.SaveConfig.instance ?: throw RuntimeException("SaveConfig not initialized")

        object Config {
            var debugEnabled = false
            var explosionEnable = true
            var noSymbols = false
            var cablePowerFactor = 1.0
            var killMonstersAroundLamps = false
            var debugExplosions = false
        }
        val config = Config

        var isDevelopmentRun = false
    }
}

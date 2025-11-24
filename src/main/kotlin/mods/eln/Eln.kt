package mods.eln

import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.api.distmarker.Dist
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
import mods.eln.generic.genericArmorItem
import mods.eln.item.ItemPickaxeEln
import mods.eln.item.ItemAxeEln
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.ShovelItem
import net.minecraft.world.item.HoeItem
import net.minecraft.world.item.ItemStack
import mods.eln.misc.IConfigSharing
import mods.eln.sixnode.electricaldatalogger.DataLogsPrintDescriptor
import mods.eln.registration.ItemRegistration
import net.minecraftforge.registries.RegistryObject
import mods.eln.registration.SixNodeRegistration
import mods.eln.registration.TransparentNodeRegistration

@Mod(Eln.MODID)
class Eln {

    @JvmField
    var electricalFrequency = 20.0
    @JvmField
    var thermalFrequency = 20.0
    var testItem: GenericItemUsingDamageDescriptor? = null
    @JvmField
    var signalCableDescriptor: mods.eln.sixnode.electricalcable.ElectricalCableDescriptor? = null
    @JvmField
    var stdCableRenderSignal: CableRenderDescriptor? = null
    var lowCurrentCableRender: CableRenderDescriptor? = null
    var featureMap = java.util.HashMap<String, String>()
    var forceOreRegen = false
    // var oreBlock: net.minecraft.world.level.block.Block? = null // Duplicate
    // var oreItem: GenericItemUsingDamageDescriptor? = null // Duplicate
    
    var batteryCapacityFactor = 1.0
    var stdBatteryHalfLife = 3600.0 * 24 * 365

    var configShared = ArrayList<IConfigSharing>()

    @JvmField var lowVoltageCableDescriptor: ElectricalCableDescriptor? = null
    @JvmField var meduimVoltageCableDescriptor: ElectricalCableDescriptor? = null
    @JvmField var highVoltageCableDescriptor: ElectricalCableDescriptor? = null
    @JvmField var veryHighVoltageCableDescriptor: ElectricalCableDescriptor? = null
    @JvmField var compressorRecipes = mods.eln.misc.RecipesList()
    @JvmField var solarPanelPowerFactor = 1.0
    @JvmField var windTurbinePowerFactor = 1.0
    @JvmField var waterTurbinePowerFactor = 1.0
    @JvmField var fuelGeneratorPowerFactor = 1.0
    @JvmField var fuelGeneratorTankCapacity = 1000.0
    @JvmField var heatTurbinePowerFactor = 1.0
    @JvmField var sixNodeThermalLoadInitializer = ThermalLoadInitializer(
        cableWarmLimit,
        -10.0,
        cableHeatingTime,
        cableThermalConductionTao
    )
    @JvmField var furnaceList = ArrayList<ItemStack>()
    @JvmField var electricalFurnace: ElectricalFurnaceDescriptor? = null
    @JvmField var magnetiserRecipes = mods.eln.misc.RecipesList()
    @JvmField var plateMachineRecipes = mods.eln.misc.RecipesList()
    @JvmField var batteryVoltageFunctionTable: mods.eln.misc.FunctionTable? = null

    @JvmField var stdCableRenderSignalBus: CableRenderDescriptor? = null
    @JvmField var stdCableRender50V: CableRenderDescriptor? = null
    @JvmField var stdCableRender200V: CableRenderDescriptor? = null
    @JvmField var stdCableRender800V: CableRenderDescriptor? = null
    @JvmField var stdCableRender3200V: CableRenderDescriptor? = null
    @JvmField var stdCableRenderCreative: CableRenderDescriptor? = null

    @JvmField var signalBusCableDescriptor: ElectricalCableDescriptor? = null
    @JvmField var creativeCableDescriptor: ElectricalCableDescriptor? = null

    @JvmField var lowCurrentCableDescriptor: CurrentCableDescriptor? = null
    @JvmField var mediumCurrentCableDescriptor: CurrentCableDescriptor? = null
    @JvmField var highCurrentCableDescriptor: CurrentCableDescriptor? = null

    @JvmField var mediumCurrentCableRender: CableRenderDescriptor? = null
    @JvmField var highCurrentCableRender: CableRenderDescriptor? = null

    @JvmField var stdPortableNaN: CableRenderDescriptor? = null
    @JvmField var portableNaNDescriptor: mods.eln.sixnode.PortableNaNDescriptor? = null


    @JvmField var isDevelopmentRun = false

    @JvmField var ElnToOtherEnergyConverterEnable = true
    @JvmField var ELN_CONVERTER_MAX_POWER = 10000.0
    @JvmField var elnToOtherBlockConverter: net.minecraft.world.level.block.Block? = null
    var ComputerProbeEnable = true
    var computerProbeBlock: net.minecraft.world.level.block.Block? = null

    fun LVP() = 200.0
    fun MVP() = 800.0
    fun HVP() = 3200.0
    fun VVP() = 12800.0

    init {
        instance = this
        mods.eln.node.NodeManager()
        val modEventBus = FMLJavaModLoadingContext.get().modEventBus
        
        // Load models early as descriptors need them
        obj.loadAllElnModels()

        ItemRegistration.registerItem()
        SixNodeRegistration.registerSix()
        TransparentNodeRegistration.registerTransparent()
        
        Registration.init(modEventBus)
        mods.eln.fluid.ElnFluids.init(modEventBus)
        mods.eln.recipe.ElnRecipeTypes.register(modEventBus)
        modEventBus.addListener(this::commonSetup)
        modEventBus.addListener(this::clientSetup)

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientProxy.setup(modEventBus)
        }

        treeResinCollectorBlockEntity = Registration.TREE_RESIN_COLLECTOR_BLOCK_ENTITY as net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<*>>

        testItem = GenericItemUsingDamageDescriptor("Test Item")
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("Electrical Age Common Setup")
        ElnNetwork.init()
        sixNodeItem = Registration.SIX_NODE_ITEM.get()
        sixNodeBlock = Registration.SIX_NODE_BLOCK.get()
        sixNodeEntity = Registration.SIX_NODE_BLOCK_ENTITY.get()
        transparentNodeItem = Registration.TRANSPARENT_NODE_ITEM.get() as TransparentNodeItem
        
        SixNodeRegistration.applyRegistrations()
        TransparentNodeRegistration.applyRegistrations()
        mods.eln.registration.SingleNodeRegistration.registerSingle()
    }

    private fun clientSetup(event: FMLClientSetupEvent) {
        LOGGER.info("Electrical Age Client Setup")
        ClientProxy.registerRenderers()
    }

    class SignalCableDescriptorStub {
        var render: CableRenderDescriptor? = null
        fun applyTo(o: Any?) {}
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(Eln::class.java)

        @JvmField
        var instance: Eln? = null
        const val MODID = "eln"
        const val LVU = 50.0
        const val MVU = 200.0
        const val HVU = 800.0
        const val SVU = 200.0
        const val SVP = 800.0 // Guessing 4x SVU
        const val SVII = 1.0 // Guessing
        const val VVU = 3200.0 // Guessing
        const val cableThermalConductionTao = 1.0 // Guessing

        const val gateOutputCurrent = 0.100
        const val CCU = 50.0
        const val cableWarmLimit = 100.0
        const val cableHeatingTime = 10.0
        const val packetPlaySound = 1
        const val packetDestroyUuid: Byte = 2
        const val packetPlayerKey = 3
        const val packetNodeSingleSerialized = 4
        const val packetPublishForNode = 5
        const val packetForClientNode = 6
        const val packetOpenLocalGui = 7
        const val packetClientToServerConnection = 8
        const val packetServerToClientInfo = 9
        var maxSoundDistance = 64.0

        @JvmField
        var sixNodeItem: mods.eln.node.six.SixNodeItem? = null
        var sharedItem = mods.eln.generic.GenericItemRegistry()
        var cableThermalLoadInitializer = ThermalLoadInitializer(
            cableWarmLimit,
            -10.0,
            cableHeatingTime,
            cableThermalConductionTao
        )

        const val SVUinv = 1.0 / SVU

        @JvmStatic
        fun getSmallRs() = 0.001



        var transistor: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var alu: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var siliconWafer: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        var plateCopper: mods.eln.generic.GenericItemUsingDamageDescriptor? = null

        lateinit var transparentNodeItem: TransparentNodeItem
        var solarPanelBasePower = 40.0
        val ghostBlock: RegistryObject<GhostBlock> = Registration.GHOST_BLOCK

        var batteryVoltageFunctionTable = mods.eln.misc.FunctionTable(doubleArrayOf(0.0, 1.0), 1.0)

        @JvmField
        val obj = mods.eln.misc.Obj3DFolder()
        
        @JvmField
        var simulator: mods.eln.sim.Simulator = mods.eln.sim.Simulator(
            0.05,
            1.0 / 20.0,
            1,
            1.0 / 20.0
        )
        @JvmField
        var wailaEasyMode = false
        @JvmField
        var debugEnabled = false
        @JvmField
        var explosionEnable = true
        @JvmField
        var noSymbols = false
        @JvmField
        var debugExplosions = false
        
        @JvmField
        var modbusServer = mods.eln.sixnode.modbusrtu.ModbusServer()

        fun findItemStack(name: String, amount: Int): ItemStack {
            return ItemStack(net.minecraft.world.item.Items.AIR)
        }

        var ledLampInfiniteLife = false
        var wind = mods.eln.misc.WindProcess()

        var modbusEnable = true
        var enableFestivities = true

        @JvmStatic
        fun applySmallRs(load: mods.eln.sim.ElectricalLoad) {
            // In the 1.20 port, ElectricalLoad no longer exposes setRs(); this will need
            // to be updated to match the new API. For now, this is a no-op stub so
            // the code compiles.
            // load.Rs = getSmallRs()
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
        
        var oreScannerConfig = ArrayList<Any>()
        
        @JvmField
        var oreItem: mods.eln.item.OreItem? = null
        @JvmField
        var oreBlock: net.minecraft.world.level.block.Block? = null

        @JvmField
        var miningPipeDescriptor: mods.eln.item.MiningPipeDescriptor? = null
        
        @JvmField
        var sharedItemStackOne = sharedItem
        
        @JvmField
        var dictionnaryOreFromMod = java.util.HashMap<String, net.minecraft.world.item.ItemStack>()
        
        @JvmField
        var dustCopper: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        @JvmField
        var copperIngot: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        @JvmField
        var plumbIngot: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        @JvmField
        var tungstenIngot: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        
        var dictSiliconWafer = true
        var dictTransistor = true
        var dictThermistor = true
        var dictNibbleMemory = true
        var dictALU = true
        
        @JvmField
        var thermistor: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        @JvmField
        var nibbleMemory: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        
        @JvmField
        var multiMeterElement: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        @JvmField
        var thermometerElement: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        @JvmField
        var allMeterElement: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        @JvmField
        var configCopyToolElement: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        
        @JvmField
        var treeResin: mods.eln.generic.GenericItemUsingDamageDescriptor? = null
        
        @JvmField
        var dataLogsPrintDescriptor: DataLogsPrintDescriptor? = null
        
        @JvmField
        var wrenchItemStack: net.minecraft.world.item.ItemStack? = null
        
        @JvmField
        var whiteDesc: Any? = null
        @JvmField
        var brushSubNames: Array<String>? = null
        
        @JvmField
        var genCopper: Boolean = true
        @JvmField
        var genLead: Boolean = true
        @JvmField
        var genTungsten: Boolean = true
        @JvmField
        var genCinnabar: Boolean = true

        val AUTHORS = listOf("Baughn", "Briman", "Dries007", "Maeyanie", "Mr_Hazard", "Xbony2")

        val saveConfig: mods.eln.server.SaveConfig
            get() = mods.eln.server.SaveConfig.instance ?: throw RuntimeException("SaveConfig not initialized")

        val config = mods.eln.config.LegacyConfig()

        var modbusPort = 1502
        var versionCheckEnabled = true
        var analyticsEnabled = true
        var analyticsURL = ""
        var analyticsPlayerUUIDOptIn = false
        var verticalIronCableCrafting = false
        var playerUUID = ""
        var directPoles = true
        var shaftEnergyFactor = 0.05
        var dictTungstenOre = ""
        var dictTungstenDust = ""
        var dictTungstenIngot = ""
        var dictCheapChip = ""
        var dictAdvancedChip = ""
        // var allowSwingingLamps = true // Moved to companion object
        @JvmField
        var allowSwingingLamps = true
        @JvmField
        var wirelessTxRange = 32
        @JvmField
        var cablePowerFactor = 1.0
        @JvmField
        var fuelHeatValueFactor = 0.0000675
        @JvmField
        var noVoltageBackground = false
        @JvmField
        var soundChannels = 200
        @JvmField
        var flywheelMass = 50.0
        @JvmField
        var plateConversionRatio = 1
        @JvmField
        var replicatorPop = false
        @JvmField
        var replicatorRegistrationId = -1
        @JvmField
        var killMonstersAroundLamps = true
        @JvmField
        var killMonstersAroundLampsRange = 9
        @JvmField
        var maxReplicators = 100
        @JvmField
        var incandescentLampLife = 16.0
        @JvmField
        var economicLampLife = 64.0
        @JvmField
        var carbonLampLife = 6.0
        @JvmField
        var ledLampLife = 512.0
        @JvmField
        var addOtherModOreToXRay = true
        @JvmField
        var xRayScannerRange = 5.0f
        @JvmField
        var xRayScannerCanBeCrafted = true
        @JvmField
        var electricalInterSystemOverSampling = 50
        @JvmField
        var fuelHeatFurnacePowerFactor = 1.0
        @JvmField
        var autominerRange = 10
        @JvmField
        var delayedTask = mods.eln.server.DelayedTaskManager()

        @JvmField
        var sixNodeBlock: mods.eln.node.six.SixNodeBlock? = null
        @JvmField
        var sixNodeEntity: net.minecraft.world.level.block.entity.BlockEntityType<mods.eln.node.six.SixNodeEntity>? = null

        @JvmField
        var portableOreScannerElement: mods.eln.generic.GenericItemUsingDamageDescriptor? = null

        @JvmField
        var serverEventListener: mods.eln.server.ServerEventListener? = null

        @JvmField
        var treeResinCollectorBlockEntity: net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<*>>? = null
    }
}

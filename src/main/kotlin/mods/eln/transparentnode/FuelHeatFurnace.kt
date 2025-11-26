package mods.eln.transparentnode

import mods.eln.Eln
import mods.eln.fluid.FuelRegistry
import mods.eln.fluid.PreciseElementFluidHandler
import mods.eln.generic.GenericItemUsingDamageSlot
import mods.eln.gui.*
import mods.eln.gui.ISlotSkin.SlotSkin
import mods.eln.i18n.I18N.tr
import mods.eln.item.regulator.IRegulatorDescriptor
import mods.eln.item.RegulatorSlot
import mods.eln.item.FuelBurnerDescriptor
import mods.eln.misc.*
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.*
import mods.eln.node.NodeBase
import mods.eln.sim.ThermalLoadInitializerByPowerDrop
import mods.eln.sim.nbt.NbtThermalLoad
import mods.eln.sim.nbt.NbtElectricalGateInput
import mods.eln.node.published
import net.minecraft.server.level.ServerPlayer
import mods.eln.sim.RegulatorProcess
import net.minecraftforge.network.NetworkHooks
import mods.eln.sim.process.destruct.ThermalLoadWatchDog
import mods.eln.node.NodePeriodicPublishProcess
import mods.eln.generic.GenericItemUsingDamageDescriptor
import mods.eln.sound.LoopedSound
import mods.eln.sim.process.destruct.WorldExplosion
import net.minecraft.client.gui.components.Button
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import net.minecraft.network.chat.Component
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.network.FriendlyByteBuf
import mods.eln.init.Registration
import net.minecraft.world.SimpleContainer
import net.minecraft.core.BlockPos

class FuelHeatFurnaceDescriptor(name: String, model: Obj3D, val thermal: ThermalLoadInitializerByPowerDrop) :
    TransparentNodeDescriptor(name, FuelHeatFurnaceElement::class.java, FuelHeatFurnaceRender::class.java,
        EntityMetaTag.Fluid) {
    private val main = model.getPart("Main")
    private val burners = arrayOf(model.getPart("BurnerA"), model.getPart("BurnerB"), model.getPart("BurnerC"))
    private val powerLED = model.getPart("PowerLED")
    private val heatLED = model.getPart("HeatLED")

    init {
        thermal.setMaximalPower(2000.0)
        voltageLevelColor = VoltageLevelColor.Thermal
    }

    fun draw(installedBurner: Int? = null, on: Boolean = false, heating: Boolean = false) {
        main?.draw()
        if (installedBurner != null) {
            burners[installedBurner]?.draw()
        }

        if (on) {
            GL11.glColor3f(0f, 1f, 0f)
            UtilsClient.drawLight(powerLED)
        } else {
            GL11.glColor3f(0f, 0.5f, 0f)
            powerLED?.draw()
        }

        if (heating) {
            GL11.glColor3f(1f, 0f, 0f)
            UtilsClient.drawLight(heatLED)
        } else {
            GL11.glColor3f(0.5f, 0f, 0f)
            heatLED?.draw()
        }
        GL11.glColor3f(1f, 1f, 1f)
    }

    fun draw(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int, installedBurner: Int? = null, on: Boolean = false, heating: Boolean = false) {
        main?.draw(poseStack, bufferSource, packedLight, packedOverlay)
        if (installedBurner != null) {
            burners[installedBurner]?.draw(poseStack, bufferSource, packedLight, packedOverlay)
        }

        if (on) {
            UtilsClient.drawLight(powerLED, poseStack, bufferSource, packedLight, packedOverlay, 0f, 1f, 0f, 1f)
        } else {
            powerLED?.drawColored(poseStack, bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityCutout(powerLED.textureResource ?: UtilsClient.whiteTexture)), packedLight, packedOverlay, 0, 128, 0, 255)
        }

        if (heating) {
            UtilsClient.drawLight(heatLED, poseStack, bufferSource, packedLight, packedOverlay, 1f, 0f, 0f, 1f)
        } else {
            heatLED?.drawColored(poseStack, bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityCutout(heatLED.textureResource ?: UtilsClient.whiteTexture)), packedLight, packedOverlay, 128, 0, 0, 255)
        }
    }

    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(Component.literal(tr("Generates heat when supplied with fuel.")))
        list.add(Component.literal(Utils.plotCelsius(tr("  Max. temperature: "), thermal.maximumTemperature)))
    }
}

class FuelHeatFurnaceElement(transparentNode: TransparentNode, descriptor: TransparentNodeDescriptor) :
    TransparentNodeElement(transparentNode, descriptor) {
    companion object {
        val ExternalControlledToggleEvent: Byte = 0
        val MainSwitchToggleEvent: Byte = 1
        val SetManualControlValueEvent: Byte = 2
        val SetTemperatureEvent: Byte = 3
    }

    val thermalLoad = NbtThermalLoad("thermalLoad")
    val controlLoad = NbtElectricalGateInput("commandLoad")

    private val tank = PreciseElementFluidHandler(25)

    override val inventory = TransparentNodeElementInventory(2, 1, this)

    var externalControlled by published(false)
    var mainSwitch by published(false)

    var manualControl by published(0.0)
    var setTemperature by published(0.0)

    var heaterControlValue = 0.0
    var actualHeatPower by published(0.0)

    private val controlProcess = object : RegulatorProcess("controller") {
        override fun process(time: Double) {
            val nominalPower = if (mainSwitch)
                FuelBurnerDescriptor.getDescriptor(inventory.getItem(FuelHeatFurnaceContainer.FuelBurnerSlot))?.producedHeatPower ?: 0.0
            else
                0.0

            when {
                externalControlled -> {
                    setCmd(controlLoad.voltage / Eln.SVU)
                }
                else -> {
                    setCmd(manualControl)
                }
            }
            // Call parent process for PID control logic
            super.process(time)

            val availableEnergy = tank.drainEnergy(heaterControlValue * nominalPower * time)
            actualHeatPower = availableEnergy / time
            thermalLoad.PcTemp += actualHeatPower
        }

        override fun getHit() = thermalLoad.temperatureCelsius
        override fun setCmd(cmd: Double) {
            heaterControlValue = if (cmd > 0.0) cmd else 0.0
        }
    }

    private val thermalWatchdog = ThermalLoadWatchDog(thermalLoad)

    init {
        thermalLoadList.add(thermalLoad)
        thermalFastProcessList.add(controlProcess)
        electricalLoadList.add(controlLoad)
        slowProcessList.add(NodePeriodicPublishProcess(transparentNode, 2.0, 1.0))
        slowProcessList.add(thermalWatchdog)

        tank.setFilter(FuelRegistry.fluidListToFluids(FuelRegistry.gasolineList + FuelRegistry.dieselList))

        thermalWatchdog.setTemperatureLimits((descriptor as FuelHeatFurnaceDescriptor).thermal)
            .setDestroys(WorldExplosion(this).machineExplosion())
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU) = when {
        side != front.inverse() && lrdu == LRDU.Down -> controlLoad
        else -> null
    }

    override fun getThermalLoad(side: Direction, lrdu: LRDU) = when {
        side == front.inverse() && lrdu == LRDU.Down -> thermalLoad
        else -> null
    }

    override fun getConnectionMask(side: Direction, lrdu: LRDU) = when (lrdu) {
        LRDU.Down -> when (side) {
            front.inverse() -> NodeBase.maskThermal
            else -> NodeBase.maskElectricalInputGate
        }
        else -> 0
    }

    override fun getFluidHandler() = tank

    override fun multiMeterString(side: Direction) = Utils.plotPower("P:", thermalLoad.power)

    override fun thermoMeterString(side: Direction) = Utils.plotCelsius("T:", thermalLoad.temperatureCelsius)

    override fun initialize() {
        (descriptor as FuelHeatFurnaceDescriptor).thermal.applyToThermalLoad(thermalLoad)
        inventoryChange(inventory)
        connect()
    }

    override fun onBlockActivated(player: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        if (player.level().isClientSide) return true
        
        if (player is ServerPlayer) {
            NetworkHooks.openScreen(player, object : MenuProvider {
                override fun createMenu(id: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
                    return FuelHeatFurnaceContainer(node, player, this@FuelHeatFurnaceElement.inventory!!, id)
                }

                override fun getDisplayName(): Component {
                    return Component.literal("Fuel Heat Furnace")
                }
            }, node!!.coordinate.toBlockPos())
        }
        return true
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        stream.writeBoolean(externalControlled)
        stream.writeBoolean(mainSwitch)
        stream.writeFloat(heaterControlValue.toFloat())
        stream.writeFloat(setTemperature.toFloat())
        stream.writeFloat(actualHeatPower.toFloat())
        stream.writeFloat(thermalLoad.temperatureCelsius.toFloat())
        stream.writeInt(FuelBurnerDescriptor.getDescriptor(inventory.getItem(FuelHeatFurnaceContainer.FuelBurnerSlot))?.type ?: -1)
    }

    override fun networkUnserialize(stream: DataInputStream): Byte {
        when (super.networkUnserialize(stream)) {
            ExternalControlledToggleEvent -> {
                externalControlled = !externalControlled
                inventoryChange(inventory)
                needPublish()
            }
            MainSwitchToggleEvent -> {
                mainSwitch = !mainSwitch
                needPublish()
            }
            SetManualControlValueEvent -> {
                manualControl = stream.readFloat().toDouble()
                needPublish()
            }
            SetTemperatureEvent -> {
                setTemperature = stream.readFloat().toDouble()
                controlProcess.target = setTemperature
                needPublish()
            }
        }

        return unserializeNulldId
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        tank.writeToNBT(nbt, "tank")
        nbt.putBoolean("externalControlled", externalControlled)
        nbt.putBoolean("mainSwitch", mainSwitch)
        nbt.putDouble("heaterControlValue", heaterControlValue)
        nbt.putDouble("manualControl", manualControl)
        nbt.putDouble("setTemperature", setTemperature)
        nbt.putDouble("actualHeatPower", actualHeatPower)
        // Note: controlProcess is saved by super.writeToNBT via thermalFastProcessList
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        tank.readFromNBT(nbt, "tank")
        externalControlled = nbt.getBoolean("externalControlled")
        mainSwitch = nbt.getBoolean("mainSwitch")
        heaterControlValue = nbt.getDouble("heaterControlValue")
        manualControl = nbt.getDouble("manualControl")
        setTemperature = nbt.getDouble("setTemperature")
        actualHeatPower = nbt.getDouble("actualHeatPower")
        // Note: controlProcess is loaded by super.readFromNBT via thermalFastProcessList
        // We need to restore the target which may be loaded from a different NBT key
        controlProcess.target = setTemperature
    }

    override fun hasGui() = true

    override fun getWaila(): MutableMap<String, String> {
        val info = HashMap<String, String>()
        info[tr("Temperature")] = Utils.plotCelsius("", thermalLoad.temperatureCelsius)
        info[tr("Power")] = Utils.plotPower("", actualHeatPower)
        return info
    }

    override fun inventoryChange(inventory: Container?) {
        val hasBurner = inventory?.getItem(FuelHeatFurnaceContainer.FuelBurnerSlot)?.isEmpty == false
        mainSwitch = mainSwitch && hasBurner

        val regulatorStack = inventory?.getItem(FuelHeatFurnaceContainer.RegulatorSlot)
        if (regulatorStack != null && !regulatorStack.isEmpty && !externalControlled) {
            // Try multiple lookup methods to find the regulator descriptor
            val regulator = (Eln.sharedItem.getDescriptor(regulatorStack) as? IRegulatorDescriptor)
                ?: (GenericItemUsingDamageDescriptor.getDescriptor(regulatorStack) as? IRegulatorDescriptor)
            regulator?.applyTo(controlProcess, 500.0, 20.0, 0.2, 0.1)
        } else {
            controlProcess.setManual()
        }
    }

    override fun newContainer(side: Direction, player: Player) = FuelHeatFurnaceContainer(node, player, inventory)
}

class FuelHeatFurnaceRender(tileEntity: TransparentNodeBlockEntity, descriptor: TransparentNodeDescriptor) :
    TransparentNodeElementRender(tileEntity, descriptor) {
    override val inventory = TransparentNodeElementInventory(2, 1, this)

    var type: Int? = null
    var externalControlled = false
    var mainSwitch = false

    var manualControl = Synchronizable(0f)
    var setTemperature = Synchronizable(0f)

    var heatPower = 0f
    var actualTemperature = 0f

    val sound = object : LoopedSound("eln:fuelheatfurnace", coordinate()) {
        override fun getPitch() = FuelBurnerDescriptor.pitchForType(type)
        override fun getVolume() = if (heatPower > 0) 0.01f + 0.00001f * heatPower else 0f
    }

    init {
        addLoopedSound(sound)
    }

    override fun draw() {
        front!!.glRotateXnRef()
        (transparentNodedescriptor as FuelHeatFurnaceDescriptor).draw(type, mainSwitch, heatPower != 0f)
    }

    override fun render(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        front!!.rotateXnRef(poseStack)
        (transparentNodedescriptor as FuelHeatFurnaceDescriptor).draw(poseStack, bufferSource, packedLight, packedOverlay, type, mainSwitch, heatPower != 0f)
    }

    override fun newGuiDraw(side: Direction, player: Player) = null

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        externalControlled = stream.readBoolean()
        mainSwitch = stream.readBoolean()
        manualControl.value = stream.readFloat()
        setTemperature.value = stream.readFloat()
        heatPower = stream.readFloat()
        actualTemperature = stream.readFloat()
        type = stream.readInt().let { type ->
            when (type) {
                -1 -> null
                else -> type
            }
        }
    }
}

class FuelHeatFurnaceContainer(val base: NodeBase?, player: Player, inventory: Container, windowId: Int = 0) :
    BasicContainer(player, inventory,
        arrayOf(GenericItemUsingDamageSlot(inventory, FuelBurnerSlot, 26, 58, 1, arrayOf(FuelBurnerDescriptor::class.java),
            SlotSkin.medium, arrayOf(tr("Fuel burner slot"))),
            RegulatorSlot(inventory, RegulatorSlot, 8, 58, 1, arrayOf(IRegulatorDescriptor.RegulatorType.Analog),
                SlotSkin.medium, tr("Analog regulator slot"))), Registration.FUEL_HEAT_FURNACE_MENU.get(), windowId), INodeContainer {
    
    var pos: BlockPos? = base?.coordinate?.toBlockPos()

    companion object {
        val FuelBurnerSlot = 0
        val RegulatorSlot = 1

        fun create(windowId: Int, inv: Inventory, data: FriendlyByteBuf): FuelHeatFurnaceContainer {
            val pos = data.readBlockPos()
            val level = inv.player.level()
            val entity = level.getBlockEntity(pos) as? TransparentNodeBlockEntity
            val node = entity?.internalNode
            
            // On client, get inventory from render; on server, node will provide it
            val clientInventory = (entity?.elementRender as? FuelHeatFurnaceRender)?.inventory ?: SimpleContainer(2)
            
            val container = FuelHeatFurnaceContainer(node, inv.player, clientInventory, windowId)
            container.pos = pos
            return container
        }
    }

    override val node = base

    override val refreshRateDivider = 1

    val element: FuelHeatFurnaceElement?
        get() = (base as? TransparentNode)?.element as? FuelHeatFurnaceElement

    val data = object : net.minecraft.world.inventory.ContainerData {
        override fun get(index: Int): Int {
            val e = element ?: return 0
            return when (index) {
                0 -> if (e.externalControlled) 1 else 0
                1 -> if (e.mainSwitch) 1 else 0
                2 -> (e.heaterControlValue * 1000).toInt()
                3 -> (e.manualControl * 1000).toInt()
                4 -> (e.setTemperature * 10).toInt()
                5 -> (e.actualHeatPower * 10).toInt()
                6 -> (e.thermalLoad.temperatureCelsius * 10).toInt()
                7 -> FuelBurnerDescriptor.getDescriptor(e.inventory.getItem(FuelBurnerSlot))?.type ?: -1
                8 -> if (e.inventory.getItem(RegulatorSlot).isEmpty) 0 else 1
                else -> 0
            }
        }
        override fun set(index: Int, value: Int) {
            // ContainerData.set is called client-side when receiving sync from server
            // We don't actually set anything here - the server is the source of truth
        }
        override fun getCount(): Int = 9
    }

    init {
        addDataSlots(data)
    }
}

class FuelHeatFurnaceGui(menu: FuelHeatFurnaceContainer, playerInventory: Inventory, title: Component) :
    GuiContainerEln<FuelHeatFurnaceContainer>(menu, playerInventory, title) {
    
    // Store player reference for convenience
    private val player = playerInventory.player
    
    // Get render dynamically - it may become available after GUI opens
    private val render: FuelHeatFurnaceRender?
        get() = menu.pos?.let { 
            (player.level().getBlockEntity(it) as? TransparentNodeBlockEntity)?.elementRender as? FuelHeatFurnaceRender 
        }

    lateinit var externalControlled: Button
    lateinit var mainSwitch: Button

    lateinit var manualControl: GuiVerticalTrackBar
    lateinit var setTemperature: GuiVerticalTrackBarHeat
    private val slotSize = 18
    
    private var isDraggingManualControl = false
    private var isDraggingSetTemperature = false

    override fun initGui() {
        super.initGui()

        externalControlled = addRenderableWidget(Button.builder(Component.literal("")) {
            render?.clientSendId(FuelHeatFurnaceElement.ExternalControlledToggleEvent)
        }.bounds(leftPos + 6, topPos + 6, 100, 20).build())

        mainSwitch = addRenderableWidget(Button.builder(Component.literal("")) {
            render?.clientSendId(FuelHeatFurnaceElement.MainSwitchToggleEvent)
        }.bounds(leftPos + 6, topPos + 30, 100, 20).build())

        manualControl = GuiVerticalTrackBar(144, 8, 20, 69)
        manualControl.setStepIdMax((0.9f / 0.01f).toInt())
        manualControl.setRange(0f, 1f)
        manualControl.value = render?.manualControl?.value ?: 0f

        setTemperature = GuiVerticalTrackBarHeat(116, 8, 20, 69)
        setTemperature.setStepIdMax(98)
        setTemperature.setRange(0f, 900f)
        setTemperature.value = render?.setTemperature?.value ?: 0f
    }

    override fun preDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        super.preDraw(guiGraphics, f, x, y)

        val externalControlledVal = menu.data.get(0) != 0
        val mainSwitchVal = menu.data.get(1) != 0
        val manualControlVal = menu.data.get(3) / 1000.0
        val setTemperatureVal = menu.data.get(4) / 10.0
        val actualHeatPowerVal = menu.data.get(5) / 10.0
        val temperatureCelsiusVal = menu.data.get(6) / 10.0
        val burnerType = menu.data.get(7)

        if (!externalControlledVal)
            externalControlled.message = Component.literal(tr("Internal control"))
        else
            externalControlled.message = Component.literal(tr("External control"))

        if (mainSwitchVal)
            mainSwitch.message = Component.literal(tr("Furnace is on"))
        else
            mainSwitch.message = Component.literal(tr("Furnace is off"))
        mainSwitch.active = burnerType >= 0

        val hasRegulator = menu.data.get(8) != 0
        
        if (!isDraggingManualControl) {
            manualControl.value = manualControlVal.toFloat()
        }
        // Manual control is enabled when NO regulator is installed and not externally controlled
        manualControl.setEnable(!hasRegulator && !externalControlledVal)
        manualControl.setComment(0, Utils.plotPercent(tr("Control value at "), manualControl.value.toDouble()))
        manualControl.setComment(1, Utils.plotPower(tr("Heat Power: "), actualHeatPowerVal))

        if (!isDraggingSetTemperature) {
            setTemperature.value = setTemperatureVal.toFloat()
        }
        // Temperature setpoint is enabled when a regulator IS installed and not externally controlled
        setTemperature.setEnable(hasRegulator && !externalControlledVal)
        setTemperature.temperatureHit = Math.max(0.0, temperatureCelsiusVal)
        setTemperature.setComment(0, tr("Temperature"))
        setTemperature.setComment(1, Utils.plotCelsius(tr("Actual: "), temperatureCelsiusVal))
        if (!externalControlledVal)
            setTemperature.setComment(2, Utils.plotCelsius(tr("Set point: "), setTemperature.value.toDouble()))
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY)
        drawSlot(guiGraphics, leftPos + 26, topPos + 58)
        drawSlot(guiGraphics, leftPos + 8, topPos + 58)
    }

    override fun postDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        manualControl.draw(guiGraphics, leftPos, topPos)
        setTemperature.draw(guiGraphics, leftPos, topPos)
    }

    private fun drawSlot(guiGraphics: GuiGraphics, x: Int, y: Int) {
        // Vanilla-like slot frame shading to match 1.7.10 look
        val outer = 0xFFC6C6C6.toInt()
        val mid = 0xFF8B8B8B.toInt()
        val inner = 0xFF373737.toInt()
        guiGraphics.fill(x, y, x + slotSize, y + slotSize, outer)
        guiGraphics.fill(x + 1, y + 1, x + slotSize - 1, y + slotSize - 1, mid)
        guiGraphics.fill(x + 2, y + 2, x + slotSize - 2, y + slotSize - 2, inner)
    }

    private fun isHoveringSlot(mouseX: Int, mouseY: Int, slotX: Int, slotY: Int): Boolean {
        val sx = leftPos + slotX
        val sy = topPos + slotY
        return mouseX in sx until (sx + slotSize) && mouseY in sy until (sy + slotSize)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        manualControl.renderTooltip(guiGraphics, font, mouseX, mouseY, leftPos, topPos)
        setTemperature.renderTooltip(guiGraphics, font, mouseX, mouseY, leftPos, topPos)

        if (isHoveringSlot(mouseX, mouseY, 26, 58)) {
            guiGraphics.renderTooltip(font, Component.literal(tr("Fuel burner slot")), mouseX, mouseY)
        } else if (isHoveringSlot(mouseX, mouseY, 8, 58)) {
            guiGraphics.renderTooltip(font, Component.literal(tr("Analog regulator slot")), mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (manualControl.handleMouseClicked(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            isDraggingManualControl = true
            if (manualControl.pending) {
                render?.clientSendFloat(FuelHeatFurnaceElement.SetManualControlValueEvent, manualControl.value)
                manualControl.pending = false
            }
            return true
        }
        if (setTemperature.handleMouseClicked(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            isDraggingSetTemperature = true
            if (setTemperature.pending) {
                render?.clientSendFloat(FuelHeatFurnaceElement.SetTemperatureEvent, setTemperature.value)
                setTemperature.pending = false
            }
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (manualControl.handleMouseDragged(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            isDraggingManualControl = true
            if (manualControl.pending) {
                render?.clientSendFloat(FuelHeatFurnaceElement.SetManualControlValueEvent, manualControl.value)
                manualControl.pending = false
            }
            return true
        }
        if (setTemperature.handleMouseDragged(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            isDraggingSetTemperature = true
            if (setTemperature.pending) {
                render?.clientSendFloat(FuelHeatFurnaceElement.SetTemperatureEvent, setTemperature.value)
                setTemperature.pending = false
            }
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        isDraggingManualControl = false
        isDraggingSetTemperature = false
        if (manualControl.handleMouseReleased(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            if (manualControl.pending) {
                render?.clientSendFloat(FuelHeatFurnaceElement.SetManualControlValueEvent, manualControl.value)
                manualControl.pending = false
            }
            return true
        }
        if (setTemperature.handleMouseReleased(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            if (setTemperature.pending) {
                render?.clientSendFloat(FuelHeatFurnaceElement.SetTemperatureEvent, setTemperature.value)
                setTemperature.pending = false
            }
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun newHelper(): GuiHelperContainer {
        return HelperStdContainer(this)
    }
}

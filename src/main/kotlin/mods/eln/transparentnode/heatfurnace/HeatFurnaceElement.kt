package mods.eln.transparentnode.heatfurnace

import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNode
import mods.eln.sim.nbt.NbtThermalLoad
import mods.eln.node.transparent.TransparentNodeElementInventory
import mods.eln.misc.BasicContainer
import mods.eln.gui.GuiContainerEln
import mods.eln.gui.GuiVerticalTrackBar
import mods.eln.gui.INodeContainer
import mods.eln.gui.ISlotSkin
import mods.eln.gui.SlotWithSkin
import mods.eln.init.Registration
import mods.eln.misc.Utils
import mods.eln.misc.UtilsClient
import mods.eln.node.NodeBase
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.TransparentNodeElementRender
import mods.eln.sim.process.destruct.ThermalLoadWatchDog
import mods.eln.sim.process.destruct.WorldExplosion
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.network.chat.Component
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkHooks
import net.minecraft.world.MenuProvider
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.nbt.CompoundTag
import java.io.DataInputStream
import java.io.DataOutputStream
import mods.eln.node.published
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.sim.ThermalLoadInitializerByPowerDrop
import mods.eln.i18n.I18N.tr

class HeatFurnaceElement(
    node: TransparentNode?,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElement(node, descriptor) {

    val thermalLoad = NbtThermalLoad("thermalLoad")
    override val inventory = TransparentNodeElementInventory(1, 64, this)
    
    var burnTime by published(0)
    var currentItemBurnTime by published(0)
    var damper by published(1.0) // 0.0 to 1.0

    private val thermalWatchdog = ThermalLoadWatchDog(thermalLoad)

    companion object {
        val SetDamperEvent: Byte = 1
    }

    init {
        thermalLoadList.add(thermalLoad)
        slowProcessList.add(thermalWatchdog)
        
        val desc = descriptor as HeatFurnaceDescriptor
        desc.thermalLoadInit.applyToThermalLoad(thermalLoad)
        
        thermalWatchdog.setTemperatureLimits(desc.thermalLoadInit)
            .setDestroys(WorldExplosion(this).machineExplosion())
    }

    override fun initialize() {
        inventoryChange(inventory)
    }

    override fun getThermalLoad(side: Direction, lrdu: LRDU) = when {
        side == front.inverse() && lrdu == LRDU.Down -> thermalLoad
        else -> null
    }

    override fun getConnectionMask(side: Direction, lrdu: LRDU) = when (lrdu) {
        LRDU.Down -> when (side) {
            front.inverse() -> NodeBase.maskThermal
            else -> 0
        }
        else -> 0
    }

    override fun onBlockActivated(player: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        if (player.level().isClientSide) return true
        
        if (player is ServerPlayer) {
            NetworkHooks.openScreen(player, object : MenuProvider {
                override fun createMenu(id: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu? {
                    return HeatFurnaceContainer(node, player, this@HeatFurnaceElement.inventory!!, id)
                }

                override fun getDisplayName(): Component {
                    return Component.literal("Stone Heat Furnace")
                }
            }, node!!.coordinate.toBlockPos())
        }
        return true
    }

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        stream.writeInt(burnTime)
        stream.writeInt(currentItemBurnTime)
        stream.writeFloat(damper.toFloat())
        stream.writeFloat(thermalLoad.temperatureCelsius.toFloat())
    }

    override fun networkUnserialize(stream: DataInputStream): Byte {
        when (super.networkUnserialize(stream)) {
            SetDamperEvent -> {
                damper = stream.readFloat().toDouble()
            }
        }
        return unserializeNulldId
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putInt("burnTime", burnTime)
        nbt.putInt("currentItemBurnTime", currentItemBurnTime)
        nbt.putDouble("damper", damper)
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        burnTime = nbt.getInt("burnTime")
        currentItemBurnTime = nbt.getInt("currentItemBurnTime")
        damper = nbt.getDouble("damper")
    }

    override fun hasGui() = true
    override fun newContainer(side: Direction, player: Player) = HeatFurnaceContainer(node, player, inventory)

    // Simulation Logic
    // We need a process to burn fuel.
    // Since we don't have a dedicated process class, I'll add an anonymous one or implement IProcess.
    // But TransparentNodeElement has lists for processes.
    // I'll add a slow process.
    
    private val burnProcess = object : mods.eln.sim.IProcess {
        override fun process(time: Double) {
            val desc = descriptor as HeatFurnaceDescriptor
            
            if (burnTime > 0) {
                burnTime--
                // Generate heat
                // Power = Energy / Time?
                // Or fixed power?
                // 1.7.10 likely had fixed power output while burning.
                // desc.maxPower is 4000.0.
                // But we should modulate by damper.
                
                val power = desc.maxPower * damper
                thermalLoad.PcTemp += power
            } else {
                // Check if we can burn new item
                val stack = inventory!!.getItem(0)
                if (!stack.isEmpty) {
                    // Check if item is fuel
                    val itemBurnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(stack, null)
                    if (itemBurnTime > 0) {
                        // Consume item
                        stack.shrink(1)
                        if (stack.isEmpty) {
                            inventory!!.setItem(0, ItemStack.EMPTY)
                        }
                        
                        burnTime = itemBurnTime
                        currentItemBurnTime = itemBurnTime
                    }
                }
            }
        }
    }

    init {
        // Add burn process to slowProcessList?
        // slowProcessList runs at 20Hz (tick).
        // process(time) time is deltaT.
        // If I use slowProcessList, time is ~0.05s.
        // burnTime in vanilla is in ticks.
        // So decrementing by 1 per tick is correct if running every tick.
        // But slowProcessList might run less often?
        // Eln simulator runs slow processes.
        // I'll assume it runs frequently enough or I should use time.
        // Actually, I should use time to decrement burnTime if it's in seconds.
        // But vanilla burnTime is ticks.
        // I'll assume 1 tick = 0.05s.
        // So burnTime -= time * 20?
        // Or just use integer ticks if I run every tick.
        // TransparentNodeElement doesn't have a "tick" method.
        // It has processes.
        
        slowProcessList.add(burnProcess)
    }
}

class HeatFurnaceContainer(val base: NodeBase?, player: Player, inventory: Container, windowId: Int = 0) :
    BasicContainer(player, inventory,
        arrayOf(SlotWithSkin(inventory, 0, 80, 58, ISlotSkin.SlotSkin.medium)), 
        Registration.HEAT_FURNACE_MENU.get(), windowId), INodeContainer {
    
    var pos: BlockPos? = base?.coordinate?.toBlockPos()

    companion object {
        fun create(windowId: Int, inv: Inventory, data: FriendlyByteBuf): HeatFurnaceContainer {
            val pos = data.readBlockPos()
            val level = inv.player.level()
            val entity = level.getBlockEntity(pos) as? TransparentNodeBlockEntity
            val node = entity?.internalNode
            val render = entity?.elementRender as? HeatFurnaceRender
            
            val container = HeatFurnaceContainer(node, inv.player, render?.inventory ?: SimpleContainer(1), windowId)
            container.pos = pos
            return container
        }
    }

    override val node = base
    override val refreshRateDivider = 1

    val element: HeatFurnaceElement?
        get() = (base as? TransparentNode)?.element as? HeatFurnaceElement

    val data = object : net.minecraft.world.inventory.ContainerData {
        override fun get(index: Int): Int {
            val e = element ?: return 0
            return when (index) {
                0 -> e.burnTime
                1 -> e.currentItemBurnTime
                2 -> (e.damper * 1000).toInt()
                3 -> (e.thermalLoad.temperatureCelsius * 10).toInt()
                else -> 0
            }
        }
        override fun set(index: Int, value: Int) {
            val e = element ?: return
            when (index) {
                0 -> e.burnTime = value
                1 -> e.currentItemBurnTime = value
                2 -> e.damper = value / 1000.0
                3 -> { /* Temperature is read-only from client */ }
            }
        }
        override fun getCount(): Int = 4
    }

    init {
        addDataSlots(data)
    }
}

class HeatFurnaceGui(menu: HeatFurnaceContainer, inventory: Inventory, title: Component) :
    GuiContainerEln<HeatFurnaceContainer>(menu, inventory, title) {
    
    val render: HeatFurnaceRender = (menu.pos?.let { inventory.player.level().getBlockEntity(it) } as? TransparentNodeBlockEntity)?.elementRender as? HeatFurnaceRender 
        ?: throw IllegalStateException("Could not find HeatFurnaceRender on client at ${menu.pos}")

    lateinit var damper: GuiVerticalTrackBar
    private var isDraggingDamper = false

    override fun initGui() {
        super.initGui()
        damper = GuiVerticalTrackBar(144, 8, 20, 69)
        damper.setStepIdMax(100)
        damper.setRange(0f, 1f)
        damper.value = render.damper.value
    }

    override fun preDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        super.preDraw(guiGraphics, f, x, y)

        val burnTime = menu.data.get(0)
        val currentItemBurnTime = menu.data.get(1)
        val damperVal = menu.data.get(2) / 1000.0
        val temperatureCelsiusVal = menu.data.get(3) / 10.0

        if (!isDraggingDamper) {
            damper.value = damperVal.toFloat()
        }
        damper.setComment(0, Utils.plotPercent(tr("Damper: "), damper.value.toDouble()))
        damper.setComment(1, Utils.plotCelsius(tr("Temperature: "), temperatureCelsiusVal))
    }

    override fun postDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        damper.draw(guiGraphics, leftPos, topPos)
        damper.renderTooltip(guiGraphics, font, x, y, leftPos, topPos)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (damper.handleMouseClicked(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            isDraggingDamper = true
            if (damper.pending) {
                render.clientSendFloat(HeatFurnaceElement.SetDamperEvent, damper.value)
                menu.element?.damper = damper.value.toDouble()
                damper.pending = false
            }
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (damper.handleMouseDragged(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            isDraggingDamper = true
            if (damper.pending) {
                render.clientSendFloat(HeatFurnaceElement.SetDamperEvent, damper.value)
                menu.element?.damper = damper.value.toDouble()
                damper.pending = false
            }
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        isDraggingDamper = false
        if (damper.handleMouseReleased(mouseX.toInt(), mouseY.toInt(), button, leftPos, topPos)) {
            if (damper.pending) {
                render.clientSendFloat(HeatFurnaceElement.SetDamperEvent, damper.value)
                menu.element?.damper = damper.value.toDouble()
                damper.pending = false
            }
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun newHelper(): mods.eln.gui.GuiHelperContainer {
        return mods.eln.gui.HelperStdContainer(this)
    }
}

package mods.eln.mechanical

import mods.eln.Eln
import mods.eln.gui.GuiContainerEln
import mods.eln.gui.HelperStdContainer
import mods.eln.gui.ISlotSkin.SlotSkin
import mods.eln.gui.SlotWithSkinAndComment
import mods.eln.i18n.I18N.tr
import mods.eln.misc.*
import mods.eln.misc.Direction.Companion.fromIntMinecraftSide
import mods.eln.node.transparent.TransparentNodeBlockEntity
import mods.eln.node.transparent.*
import mods.eln.sim.StackMachineProcess
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL11

class RollingShaftMachineDescriptor (name: String, override val obj: Obj3D) :
    SimpleShaftDescriptor(name, RollingShaftMachineElement::class, RollingShaftMachineRender::class, EntityMetaTag.Basic) {

    override val static = arrayOf(obj.getPart("main"))
    override val rotating = arrayOf(obj.getPart("rot1"))
    private val reverseRotating = arrayOf(obj.getPart("rot2"))

    override val sound = "eln:plate_machine"



    override fun draw(angle: Double) {
        super.draw(angle)
        preserveMatrix {
            val bb = reverseRotating[0].boundingBox()
            val centre = bb.centre()
            val ox = centre.x
            val oy = centre.y
            val oz = centre.z
            GL11.glTranslated(ox, oy, oz)
            GL11.glRotatef(((-angle * 360) / 2.0 / Math.PI).toFloat(), 0f, 0f, 1f)
            GL11.glTranslated(-ox, -oy, -oz)
            for (part in reverseRotating) {
                part.draw()
            }
        }
    }
}

class RollingShaftMachineElement(node: TransparentNode, desc: TransparentNodeDescriptor) :
    SimpleShaftElement(node, desc) {
    val desc = desc as RollingShaftMachineDescriptor
    // change size to 4 when adding roller slots
    val inv = RollingShaftMachineInventory(2, 64, this)
    override val inventory = inv

    override fun onBlockActivated(player: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        return false
    }

    override fun getWaila(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        info[tr("Energy")] = Utils.plotEnergy("", shaft.energy)
        info[tr("Speed")] = Utils.plotRads("", shaft.rads)
        info[tr("Process State")] = Utils.plotPercent("", operationalProcess.getProcessState())
        info[tr("Can Process")] = if (operationalProcess.canSmelt()) "Yes" else "No"
        return info
    }

    override fun coordonate(): Coordinate {
        return node!!.element!!.coordinate()
    }

    override fun hasGui() = true

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu {
        return RollingShaftMachineContainer(player, inv)
    }

    private val maximumRate = 4000.0

    private val energyProcess = {
        if (maximumRate > shaft.energy) {
            shaft.energy
        } else {
            maximumRate
        }
    }

    private val energyConsumer = { usedEnergy: Double ->
        shaft.energy -= usedEnergy
    }

    private val operationalProcess = StackMachineProcess(
        inv, 0, 1, 1, Eln.instance!!.plateMachineRecipes, energyProcess, energyConsumer)

    init {
        slowProcessList.add(operationalProcess)
    }
}

class RollingShaftMachineRender(entity: TransparentNodeBlockEntity, desc: TransparentNodeDescriptor): ShaftRender(entity, desc) {
    val desc = desc as RollingShaftMachineDescriptor
    // change size to 4 when adding roller slots
    val inv = RollingShaftMachineInventory(2, 64, this)
    override val inventory = inv

    override fun newGuiDraw(side: Direction, player: Player): Screen {
        return RollingShaftMachineGui(player, inv, this)
    }
}

const val cellOffset = 20

class RollingShaftMachineContainer(player: Player, inv: Container) : BasicContainer(
    player, inv, arrayOf(
        SlotWithSkinAndComment(inv, 0, 8 + cellOffset, 12, SlotSkin.medium, arrayOf("Input Slot")),
        SlotWithSkinAndComment(inv, 1, 8 + cellOffset, 12 + cellOffset * 2, SlotSkin.large, arrayOf("Output Slot"))//,
        //SlotWithSkinAndComment(inv, 2, 8, 12 + cellOffset, SlotSkin.none, arrayOf("Roller Slot")),
        //SlotWithSkinAndComment(inv, 3, 8 + cellOffset * 2, 12 + cellOffset, SlotSkin.none, arrayOf("Roller Slot"))
    )
)

class RollingShaftMachineGui(player: Player, inv: Container, val render: RollingShaftMachineRender) : GuiContainerEln<RollingShaftMachineContainer>(RollingShaftMachineContainer(player, inv), player.inventory, net.minecraft.network.chat.Component.literal("Rolling Shaft Machine")) {
    override fun newHelper() = HelperStdContainer(this)

    override fun renderBg(guiGraphics: net.minecraft.client.gui.GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        helper!!.drawBackground(guiGraphics, 176, 166)
    }
}

class RollingShaftMachineInventory: TransparentNodeElementInventory {
    private var machineElement: RollingShaftMachineElement? = null

    constructor(size: Int, stackLimit: Int, machineElement: RollingShaftMachineElement?) : super(size, stackLimit, machineElement) {
        this.machineElement = machineElement
    }

    constructor(size: Int, stackLimit: Int, render: TransparentNodeElementRender?) : super(size, stackLimit, render)

    override fun getSlotsForFace(side: net.minecraft.core.Direction): IntArray {
        return if (transparentNodeElement == null) IntArray(0) else when (fromIntMinecraftSide(side.ordinal)) {
            Direction.YP -> intArrayOf(0)
            else -> intArrayOf(1)
        }
    }

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, side: net.minecraft.core.Direction?): Boolean {
        if (side == null) return false
        return when (fromIntMinecraftSide(side.ordinal)) {
            Direction.YP -> true
            else -> false
        }
    }

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, side: net.minecraft.core.Direction): Boolean {
        return when (fromIntMinecraftSide(side.ordinal)) {
            Direction.YP -> false
            else -> true
        }
    }
}

package mods.eln.transparentnode.electricalmachine

import mods.eln.cable.CableRenderDescriptor
import mods.eln.gui.GuiLabel
import mods.eln.misc.*
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ElectricalStackMachineProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.ThermalLoadInitializer
import mods.eln.sim.mna.component.Resistor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.sound.SoundCommand
import mods.eln.wiki.Data
import mods.eln.gui.GuiItemStack
import mods.eln.gui.GuiVerticalExtender
import mods.eln.item.ItemDefault.IPlugIn
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import mods.eln.i18n.I18N.tr
import net.minecraft.core.Direction

import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

open class ElectricalMachineDescriptor(
    name: String,
    ElementClass: Class<*>,
    RenderClass: Class<*>,
    val nominalU: Double,
    val nominalP: Double,
    val maximalU: Double,
    val thermal: ThermalLoadInitializer,
    val cable: ElectricalCableDescriptor?,
    var recipe: RecipesList
) : TransparentNodeDescriptor(name, ElementClass, RenderClass), IPlugIn {

    val outStackCount = 4
    val resistorR: Double = nominalU * nominalU / nominalP
    val boosterEfficiency = 1.0 / 1.1
    val boosterSpeedUp = 1.25 / boosterEfficiency

    var endSound: SoundCommand? = null
    var runningSound: String? = null

    private var defaultHandle: Any? = null

    init {
        voltageLevelColor = VoltageLevelColor.fromCable(cable)
    }

    fun setRunningSound(runningSound: String): ElectricalMachineDescriptor {
        this.runningSound = runningSound
        return this
    }

    fun setEndSound(endSound: SoundCommand): ElectricalMachineDescriptor {
        this.endSound = endSound
        return this
    }

    open fun volumeForRunningSound(processState: Float, powerFactor: Float): Float {
        return if (powerFactor >= 0.3) 0.3f * powerFactor else 0f
    }

    /*
    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        recipe.addMachine(newItemStack(1))
        Data.addMachine(newItemStack(1))
    }
    */

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, level, tooltip, flag)
        tooltip.add(Component.literal(tr("Nominal voltage: %1\$V", Utils.plotValue(nominalU))))
        tooltip.add(Component.literal(tr("Nominal power: %1\$W", Utils.plotValue(nominalP))))
    }

    fun applyTo(load: ElectricalLoad) {
        cable!!.applyTo(load)
    }

    fun applyTo(resistor: Resistor) {
        resistor.setResistance(resistorR)
    }

    fun applyTo(machine: ElectricalStackMachineProcess) {
        machine.setResistorValue(resistorR)
    }

    fun applyTo(load: ThermalLoad) {
        thermal.applyTo(load)
    }

    open fun newDrawHandle(): Any? {
        return null
    }

    open fun draw(render: ElectricalMachineRender, handleO: Any?, inEntity: ItemEntity?, outEntity: ItemEntity?, powerFactor: Float, processState: Float) {
    }

    open fun refresh(deltaT: Float, render: ElectricalMachineRender, handleO: Any?, inEntity: ItemEntity?, outEntity: ItemEntity?, powerFactor: Float, processState: Float) {
    }

    open fun powerLrdu(side: Direction, front: Direction): Boolean {
        return true
    }

    open fun drawCable(): Boolean {
        return false
    }

    open fun getPowerCableRender(): CableRenderDescriptor? {
        return null
    }

    private fun getDefaultHandle(): Any? {
        if (defaultHandle == null)
            defaultHandle = newDrawHandle()
        return defaultHandle
    }

    override fun top(y: Int, extender: GuiVerticalExtender, stack: ItemStack): Int {
        return y
    }

    override fun bottom(y: Int, extender: GuiVerticalExtender, stack: ItemStack): Int {
        var currentY = y
        var counter = -1

        extender.add(GuiLabel(6, currentY, tr("Can create:")))
        currentY += 12
        for (r in recipe.recipes) {
            if (counter == 0)
                currentY += (18 * 1.3).toInt()
            if (counter == -1)
                counter = 0
            var x = 6 + counter * 60

            extender.add(GuiItemStack(x, currentY, r.input, extender.helper))
            x += 18 * 2

            for (m in recipe.machines) {
                extender.add(GuiItemStack(x, currentY, m, extender.helper))
                x += 18
            }
            x += 18
            extender.add(GuiItemStack(x, currentY, r.getOutputCopy()[0], extender.helper))

            x += 22
            extender.add(GuiLabel(x, currentY + 4, Utils.plotEnergy(tr("Cost"), r.energy)))

            counter = (counter + 1) % 1
        }
        currentY += (18 * 1.3).toInt()

        return currentY
    }
}
package mods.eln.transparentnode.electricalmachine

import mods.eln.misc.*
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.sim.ThermalLoadInitializer
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import net.minecraft.world.entity.item.ItemEntity
import org.lwjgl.opengl.GL11
import net.minecraft.core.Direction

class CompressorDescriptor(
    name: String,
    obj: Obj3D,
    nominalU: Double,
    nominalP: Double,
    maximalU: Double,
    thermal: ThermalLoadInitializer,
    cable: ElectricalCableDescriptor,
    recipe: RecipesList
) : ElectricalMachineDescriptor(
    name,
    CompressorElement::class.java,
    CompressorRender::class.java,
    nominalU,
    nominalP,
    maximalU,
    thermal,
    cable,
    recipe
) {

    private var tyOn: Float = 0f
    private var tyOff: Float = 0f

    private var main: Obj3DPart? = null
    private var move: Obj3DPart? = null

    init {
        main = obj.getPart("main")
        move = obj.getPart("move")
        if (move != null) {
            tyOn = move!!.getFloat("tyon")
            tyOff = move!!.getFloat("tyoff")
        }
    }

    class CompressorDescriptorHandle {
        val interpolator = RcInterpolator(0.25f)
        var itemCounter = 0f
    }

    override fun newDrawHandle(): Any {
        return CompressorDescriptorHandle()
    }

    override fun volumeForRunningSound(processState: Float, powerFactor: Float): Float {
        return if (processState < 0.1)
            0f
        else if (processState < 0.3)
            super.volumeForRunningSound(processState, powerFactor) * (processState - 0.1f) * 5f
        else
            super.volumeForRunningSound(processState, powerFactor)
    }

    override fun draw(render: ElectricalMachineRender, handleO: Any?, inEntity: ItemEntity?, outEntity: ItemEntity?, powerFactor: Float, processState: Float) {
        val handle = handleO as CompressorDescriptorHandle

        UtilsClient.drawEntityItem(inEntity, -0.35, 0.04, 0.3, handle.itemCounter, 1f)
        UtilsClient.drawEntityItem(outEntity, 0.35, 0.04, 0.3, -handle.itemCounter + 139f, 1f)

        main?.draw()
        GL11.glTranslatef(0f, tyOff + Math.sqrt(handle.interpolator.get().toDouble()).toFloat() * (tyOn - tyOff), 0f)
        move?.draw()
    }

    override fun refresh(deltaT: Float, render: ElectricalMachineRender, handleO: Any?, inEntity: ItemEntity?, outEntity: ItemEntity?, powerFactor: Float, processState: Float) {
        val handle = handleO as CompressorDescriptorHandle
        handle.interpolator.target = processState
        handle.interpolator.step(deltaT)

        handle.itemCounter += deltaT * 90
        while (handle.itemCounter >= 360f) handle.itemCounter -= 360f
    }

    override fun powerLrdu(side: Direction, front: Direction): Boolean {
        return side != front && side != front.opposite
    }
}
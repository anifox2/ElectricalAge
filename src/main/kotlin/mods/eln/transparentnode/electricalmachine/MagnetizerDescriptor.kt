package mods.eln.transparentnode.electricalmachine

import mods.eln.cable.CableRenderDescriptor
import mods.eln.misc.*
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.sim.ThermalLoadInitializer
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import net.minecraft.world.entity.item.ItemEntity
import org.lwjgl.opengl.GL11

class MagnetizerDescriptor(
    name: String?,
    var obj: Obj3D,
    var nominalVoltage: Double,
    var nominalPower: Double,
    var maxPower: Double,
    var thermalLoadInitializer: ThermalLoadInitializer,
    var cableDescriptor: ElectricalCableDescriptor?,
    var recipes: RecipesList
) : ElectricalMachineDescriptor(
    name!!,
    MagnetizerElement::class.java,
    MagnetizerRender::class.java,
    nominalVoltage,
    nominalPower,
    maxPower,
    thermalLoadInitializer,
    cableDescriptor,
    recipes
) {
    private var main: Obj3D.Obj3DPart? = null
    private var rot: Obj3D.Obj3DPart? = null

    init {
        rot = obj.getPart("rot")
        main = obj.getPart("main")
    }

    class MaceratorDescriptorHandle {
        var counter = 0f
        var itemCounter = 0f
        val interpolator = RcInterpolator(0.5f)
    }

    override fun newDrawHandle(): Any {
        return MaceratorDescriptorHandle()
    }

    override fun draw(render: ElectricalMachineRender, handleO: Any?, inEntity: ItemEntity?, outEntity: ItemEntity?, powerFactor: Float, processState: Float) {
        val handle = handleO as MaceratorDescriptorHandle

        main?.draw()
        rot?.draw(handle.counter, 0f, 0f, 1f)

        GL11.glScalef(0.5f, 0.5f, 0.5f)
        UtilsClient.drawEntityItem(inEntity, 0.0, 0.25, 0.0, handle.itemCounter, 1f)
    }

    override fun refresh(deltaT: Float, render: ElectricalMachineRender, handleO: Any?, inEntity: ItemEntity?, outEntity: ItemEntity?, powerFactor: Float, processState: Float) {
        val handle = handleO as MaceratorDescriptorHandle
        handle.interpolator.target = powerFactor
        handle.interpolator.step(deltaT)
        handle.counter += deltaT * handle.interpolator.get() * 360f
        while (handle.counter >= 360f) handle.counter -= 360f

        handle.itemCounter += deltaT * 90f
        while (handle.itemCounter >= 360f) handle.itemCounter -= 360f
    }

    override fun drawCable(): Boolean {
        return true
    }

    override fun getPowerCableRender(): CableRenderDescriptor? {
        return cable?.render
    }
}
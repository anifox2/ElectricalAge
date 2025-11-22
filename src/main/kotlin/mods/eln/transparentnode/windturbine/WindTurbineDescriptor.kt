package mods.eln.transparentnode.windturbine

import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.ghost.GhostGroup
import mods.eln.misc.FunctionTable
import mods.eln.misc.Obj3D
import mods.eln.misc.UtilsClient
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item
import org.lwjgl.opengl.GL11

class WindTurbineDescriptor(
    name: String?,
    var obj: Obj3D?,
    var cable: ElectricalCableDescriptor,
    var PfW: FunctionTable,
    var nominalPower: Double,
    var nominalWind: Double,
    var maxVoltage: Double,
    var maxWind: Double,
    var offY: Int,
    var rayX: Int,
    var rayY: Int,
    var rayZ: Int,
    blockMalusMinCount: Int,
    var blockMalus: Double,
    var soundName: String,
    var nominalVolume: Float
) : TransparentNodeDescriptor(
    name,
    WindTurbineElement::class.java,
    WindTurbineRender::class.java
) {
    var main: Obj3D.Obj3DPart? = null
    var rot: Obj3D.Obj3DPart? = null
    var halo: Obj3D.Obj3DPart? = null
    var blockMalusSubCount: Int = blockMalusMinCount + 1
    var speed: Float = 0f

    init {
        this.PfW = PfW.duplicate(nominalWind, nominalPower)!!
        if (obj != null) {
            main = obj!!.getPart("main")
            rot = obj!!.getPart("rot")
            halo = obj!!.getPart("halo")
            if (rot != null) {
                speed = rot!!.getFloat("speed")
            }
        }
        voltageLevelColor = VoltageLevelColor.LowVoltage
        Data.addEnergy(getStack())
    }

    fun draw(alpha: Float) {
        blockMalusSubCount += ghostGroup?.size() ?: 0
        this.ghostGroup = ghostGroup
    }

    fun draw(alpha: Float, haloState: Boolean) {
        main?.draw()
        rot?.draw(alpha, 1f, 0f, 0f)
        if (halo != null && haloState) {
            UtilsClient.disableLight()
            UtilsClient.enableBlend()
            UtilsClient.disableCulling()
            GL11.glColor3f(1f, 0f, 0f)
            halo!!.draw()
            UtilsClient.enableCulling()
            UtilsClient.disableBlend()
            UtilsClient.enableLight()
        }
    }
}

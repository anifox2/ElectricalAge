package mods.eln.transparentnode.turret

import mods.eln.Eln
import mods.eln.misc.Obj3D
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item

class TurretDescriptor(
    name: String?,
    modelName: String
) : TransparentNodeDescriptor(
    name,
    TurretElement::class.java,
    TurretRender::class.java
) {
    var obj: Obj3D? = null
    var base: Obj3D.Obj3DPart? = null
    var turret: Obj3D.Obj3DPart? = null
    var gun: Obj3D.Obj3DPart? = null
    var properties = Properties()

    init {
        obj = Eln.obj.getObj(modelName)
        if (obj != null) {
            base = obj!!.getPart("Base")
            turret = obj!!.getPart("Turret")
            gun = obj!!.getPart("Gun")
        }
        Data.addEnergy(newItemStack())
    }

    class Properties {
        val actionAngle = 70f
        val detectionDistance = 12f
        val aimDistance = 15f
        val impulseEnergy = 1000f
        val gunMinElevation = -40f
        val gunMaxElevation = 70f
        val turretSeekAnimationSpeed = 40f
        val turretAimAnimationSpeed = 70f
        val gunArmAnimationSpeed = 3f
        val gunDisarmAnimationSpeed = 3f
        val gunAimAnimationSpeed = 70f
        val minimalVoltage = 200.0
        val minimalVoltageHysteresisFactor = 1.1
        val maximalVoltage = 1000.0
        val basePower = 10.0
        val chargePower = 100.0
        val entityDetectionInterval = 0.5
    }
}

package mods.eln.transparentnode.teleporter

import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.ghost.GhostGroup
import mods.eln.misc.Coordinate
import mods.eln.misc.Obj3D
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item

class TeleporterDescriptor(
    name: String?,
    var obj: Obj3D?,
    var cable: ElectricalCableDescriptor,
    var areaCoordinate: Coordinate,
    var lightCoordinate: Coordinate,
    var areaH: Int,
    var powerCoordinate: Array<Coordinate>,
    var ghostDoorOpen: GhostGroup,
    var ghostDoorClose: GhostGroup
) : TransparentNodeDescriptor(
    name,
    TeleporterElement::class.java,
    TeleporterRender::class.java
) {
    var main: Obj3D.Obj3DPart? = null
    var ext_control: Obj3D.Obj3DPart? = null
    var ext_power: Obj3D.Obj3DPart? = null
    var door_out: Obj3D.Obj3DPart? = null
    var door_in_charge: Obj3D.Obj3DPart? = null
    var door_in: Obj3D.Obj3DPart? = null
    var indoor_closed: Obj3D.Obj3DPart? = null
    var indoor_open: Obj3D.Obj3DPart? = null
    var outlampline0_alpha: Obj3D.Obj3DPart? = null
    var outlampline0: Obj3D.Obj3DPart? = null
    var leds = arrayOfNulls<Obj3D.Obj3DPart>(10)
    var scr0_electrictity: Obj3D.Obj3DPart? = null
    var scr1_cables: Obj3D.Obj3DPart? = null
    var scr2_transporter: Obj3D.Obj3DPart? = null
    var scr3_userin: Obj3D.Obj3DPart? = null
    var scr5_dooropen: Obj3D.Obj3DPart? = null
    var src4_doorclosed: Obj3D.Obj3DPart? = null
    var gyro_alpha: Obj3D.Obj3DPart? = null
    var gyro: Obj3D.Obj3DPart? = null
    var whiteblur: Obj3D.Obj3DPart? = null

    var chargeSound: String? = null
    var chargeVolume: Float = 0f

    init {
        if (obj != null) {
            main = obj!!.getPart("main")
            ext_control = obj!!.getPart("ext_control")
            ext_power = obj!!.getPart("ext_power")
            door_out = obj!!.getPart("door_out")
            door_in_charge = obj!!.getPart("door_in_charge")
            door_in = obj!!.getPart("door_in")
            indoor_closed = obj!!.getPart("indoor_closed")
            indoor_open = obj!!.getPart("indoor_open")
            // ... map other parts if needed
        }
    }

    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addEnergy(newItemStack())
    }

    fun setChargeSound(sound: String, volume: Float) {
        this.chargeSound = sound
        this.chargeVolume = volume
    }
}

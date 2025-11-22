package mods.eln.sixnode.lampsocket

import mods.eln.cable.CableRenderDescriptor
import mods.eln.item.LampDescriptor
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.UtilsClient
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElementInventory
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.IOException

class LampSocketRender(tileEntity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElementRender(tileEntity, side, descriptor) {

    var lampSocketDescriptor: LampSocketDescriptor = descriptor as LampSocketDescriptor
    var descriptor: LampSocketDescriptor = descriptor as LampSocketDescriptor

    override var inventory = SixNodeElementInventory(2, 64, this)
    var grounded = true
    var poweredByLampSupply = false

    var pertuVy = 0f
    var pertuPy = 0f
    var pertuVz = 0f
    var pertuPz = 0f
    var weatherAlphaZ = 0f
    var weatherAlphaY = 0f

    var channel: String? = null
    var lampDescriptor: LampDescriptor? = null
    var alphaZ = 0f
    var light: Byte = 0
    var oldLight: Byte = -1
    var paintColor = 15

    var isConnectedToLampSupply = false

    var cable: ElectricalCableDescriptor? = null

    override fun newGuiDraw(side: Direction, player: Player): Screen {
        return LampSocketGuiDraw(player, inventory, this)
    }

    override fun draw() {
        super.draw() //Colored cable only

        GL11.glRotatef(descriptor.initialRotateDeg, 1f, 0f, 0f)
        descriptor.render.draw(this)
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        try {
            val b = stream.readByte().toInt()
            grounded = (b and (1 shl 6)) != 0
            // Inventory sync logic skipped for now
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? {
        return if (cable != null) cable!!.render else null
    }
}

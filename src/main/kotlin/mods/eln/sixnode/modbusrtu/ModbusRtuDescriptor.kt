package mods.eln.sixnode.modbusrtu

import mods.eln.misc.Obj3D
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.misc.UtilsClient
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL11
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import com.mojang.math.Axis

class ModbusRtuDescriptor(name: String, override var obj: Obj3D?) : SixNodeDescriptor(name, ModbusRtuElement::class.java, ModbusRtuRender::class.java) {

    var main: Obj3DPart? = null
    var door: Obj3DPart? = null
    var led_power: Obj3DPart? = null
    var led_activity: Obj3DPart? = null
    var led_error: Obj3DPart? = null
    var display: Obj3DPart? = null
    var alphaOff: Float = 0f

    init {
        if (obj != null) {
            main = obj!!.getPart("main")
            door = obj!!.getPart("door")
            led_power = obj!!.getPart("led-power")
            led_activity = obj!!.getPart("led-activity")
            led_error = obj!!.getPart("led-error")
            display = obj!!.getPart("display")
            if (door != null) {
                alphaOff = door!!.getFloat("alphaOff")
            }
        }

        voltageLevelColor = VoltageLevelColor.SignalVoltage
    }

    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addSignal(newItemStack())
    }

    fun draw(poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, packedOverlay: Int, doorAlpha: Float, power: Boolean, activity: Boolean, error: Boolean) {
        if (main != null) main!!.draw(poseStack, buffer, packedLight, packedOverlay)
        if (door != null) {
            poseStack.pushPose()
            poseStack.mulPose(Axis.ZP.rotationDegrees(doorAlpha))
            door!!.draw(poseStack, buffer, packedLight, packedOverlay)
            poseStack.popPose()
        }
        if (power) UtilsClient.drawLight(led_power, poseStack, buffer, packedLight, packedOverlay) else if (led_power != null) led_power!!.draw(poseStack, buffer, packedLight, packedOverlay)
        if (activity) UtilsClient.drawLight(led_activity, poseStack, buffer, packedLight, packedOverlay) else if (led_activity != null) led_activity!!.draw(poseStack, buffer, packedLight, packedOverlay)
        if (error) UtilsClient.drawLight(led_error, poseStack, buffer, packedLight, packedOverlay) else if (led_error != null) led_error!!.draw(poseStack, buffer, packedLight, packedOverlay)
        if (display != null) display!!.draw(poseStack, buffer, packedLight, packedOverlay)
    }
}

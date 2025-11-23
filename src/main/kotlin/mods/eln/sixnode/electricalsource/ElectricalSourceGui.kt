package mods.eln.sixnode.electricalsource

import mods.eln.gui.GuiHelperContainer
import mods.eln.gui.ScreenEln
import mods.eln.gui.GuiTextFieldEln
import net.minecraft.network.chat.Component
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.text.ParseException

class ElectricalSourceGui(var render: ElectricalSourceRender) : ScreenEln() {

    var voltage: GuiTextFieldEln? = null

    override fun newHelper(): GuiHelperContainer {
        return GuiHelperContainer(this, 50 + 12, 12 + 12)
    }

    override fun init() {
        super.init()

        voltage = newGuiTextField(6, 6, 50)
        voltage!!.value = render.voltage.toFloat().toString()
        voltage!!.setResponder { s ->
            textFieldNewValue(voltage!!, s)
        }
    }

    fun textFieldNewValue(textField: GuiTextFieldEln, value: String) {
        val newVoltage: Float

        try {
            newVoltage = NumberFormat.getInstance().parse(value).toFloat()
        } catch (e: ParseException) {
            return
        }

        try {
            val bos = ByteArrayOutputStream()
            val stream = DataOutputStream(bos)

            render.preparePacketForServer(stream)

            stream.writeByte(ElectricalSourceElement.setVoltageId.toInt())
            stream.writeFloat(newVoltage)

            render.sendPacketToServer(bos)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

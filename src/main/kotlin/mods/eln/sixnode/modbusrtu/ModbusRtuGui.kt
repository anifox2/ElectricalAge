package mods.eln.sixnode.modbusrtu

import mods.eln.gui.GuiButtonEln
import mods.eln.gui.GuiLabel
import mods.eln.gui.GuiTextFieldEln
import mods.eln.gui.GuiVerticalExtender
import mods.eln.gui.ScreenEln
import mods.eln.i18n.I18N
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player

class ModbusRtuGui(player: Player, var render: ModbusRtuRender) : ScreenEln() {

    var station: GuiTextFieldEln? = null
    var name: GuiTextFieldEln? = null
    var extender: GuiVerticalExtender? = null

    var extenderYStart: Int = 0
    var txAddButton: GuiButtonEln? = null
    var rxAddButton: GuiButtonEln? = null

    var uuidToRxName = HashMap<Int, GuiTextFieldEln>()

    override fun init() {
        super.init()

        val y = 6
        render.rxTxChange = true
        extenderYStart = y + 6
        
        generateTxRd(0, 0)
    }

    fun generateTxRd(x: Int, y: Int) {
        uuidToRxName.clear()
        var extenderPosition = 0f
        if (extender != null) {
            extenderPosition = extender!!.getSliderPosition()
            clearWidgets()
        }

        val h = helper!!
        extender = GuiVerticalExtender(6, 6, h.xSize - 12, h.ySize - 12, h)
        addRenderableWidget(extender!!)

        var currentY = 0
        
        val title = GuiLabel(2, currentY, I18N.tr("Modbus RTU"))
        extender!!.add(title)
        currentY += 10

        station = GuiTextFieldEln(font, 2, currentY, 30, 20, Component.empty())
        if (render.station != -1)
            station!!.value = render.station.toString()
        station!!.setResponder { s -> 
            try {
                render.clientSend(ModbusRtuElement.setStation, s.toInt())
            } catch (e: NumberFormatException) {}
        }
        extender!!.add(station!!)

        name = GuiTextFieldEln(font, 2 + station!!.width + 12, currentY, 101, 20, Component.empty())
        currentY += name!!.height
        name!!.value = render.name
        name!!.setResponder { s ->
            render.clientSend(ModbusRtuElement.setName, s)
        }
        extender!!.add(name!!)

        currentY += 5

        val txLabel = GuiLabel(2, currentY + 6, I18N.tr("Wireless TX"))
        extender!!.add(txLabel)

        txAddButton = GuiButtonEln(2 + 65, currentY, 40, 20, I18N.tr("Add")) {
             render.clientSend(ModbusRtuElement.serverTxAdd, "New TX")
        }
        extender!!.add(txAddButton!!)
        currentY += 20
        
        val tempListTx = ArrayList(render.wirelessTxStatusList.values)
        while (tempListTx.isNotEmpty()) {
            var smaller = Int.MAX_VALUE
            var best: WirelessTxStatus? = null
            for (tx in tempListTx) {
                if (tx.uuid < smaller) {
                    smaller = tx.uuid
                    best = tx
                }
            }
            tempListTx.remove(best)
            val tx = best!!
            
            val txName = GuiTextFieldEln(font, 2, currentY, 80, 20, Component.empty())
            txName.value = tx.name
            txName.setResponder { s ->
                render.clientSend(ModbusRtuElement.serverTxConfig, tx.uuid, s, tx.id)
            }
            extender!!.add(txName)
            
            val txId = GuiTextFieldEln(font, 2 + 80 + 2, currentY, 30, 20, Component.empty())
            txId.value = tx.id.toString()
            txId.setResponder { s ->
                try {
                    render.clientSend(ModbusRtuElement.serverTxConfig, tx.uuid, tx.name, s.toInt())
                } catch (e: NumberFormatException) {}
            }
            extender!!.add(txId)
            
            val del = GuiButtonEln(2 + 80 + 2 + 30 + 2, currentY, 20, 20, "X") {
                render.clientSend(ModbusRtuElement.serverTxDelete, tx.uuid)
            }
            extender!!.add(del)
            
            currentY += 22
        }

        val rxLabel = GuiLabel(2, currentY + 6, I18N.tr("Wireless RX"))
        extender!!.add(rxLabel)

        rxAddButton = GuiButtonEln(2 + 65, currentY, 40, 20, I18N.tr("Add")) {
             render.clientSend(ModbusRtuElement.serverRxAdd, "New RX")
        }
        extender!!.add(rxAddButton!!)
        currentY += 20

        val tempListRx = ArrayList(render.wirelessRxStatusList.values)
        while (tempListRx.isNotEmpty()) {
            var smaller = Int.MAX_VALUE
            var best: WirelessRxStatus? = null
            for (rx in tempListRx) {
                if (rx.uuid < smaller) {
                    smaller = rx.uuid
                    best = rx
                }
            }
            tempListRx.remove(best)
            val rx = best!!
            
            val rxName = GuiTextFieldEln(font, 2, currentY, 80, 20, Component.empty())
            rxName.value = rx.name
            rxName.setResponder { s ->
                render.clientSend(ModbusRtuElement.serverRxConfig, rx.uuid, s, rx.id)
            }
            extender!!.add(rxName)
            uuidToRxName[rx.uuid] = rxName
            
            val rxId = GuiTextFieldEln(font, 2 + 80 + 2, currentY, 30, 20, Component.empty())
            rxId.value = rx.id.toString()
            rxId.setResponder { s ->
                try {
                    render.clientSend(ModbusRtuElement.serverRxConfig, rx.uuid, rx.name, s.toInt())
                } catch (e: NumberFormatException) {}
            }
            extender!!.add(rxId)
            
            val del = GuiButtonEln(2 + 80 + 2 + 30 + 2, currentY, 20, 20, "X") {
                render.clientSend(ModbusRtuElement.serverRxDelete, rx.uuid)
            }
            extender!!.add(del)
            
            currentY += 22
        }

        if (extender != null) {
            extender!!.setSliderPosition(extenderPosition)
        }
    }
    
    override fun tick() {
        super.tick()
        if (render.rxTxChange) {
            render.rxTxChange = false
            generateTxRd(0, 0)
        }
        
        for (rx in render.wirelessRxStatusList.values) {
            val tf = uuidToRxName[rx.uuid]
            if (tf != null) {
                if (rx.connected) {
                    tf.setTextColor(0x00FF00)
                } else {
                    tf.setTextColor(0xFF0000)
                }
            }
        }
    }
}

package mods.eln.sixnode

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import mods.eln.cable.CableRenderDescriptor
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.gui.*
import mods.eln.i18n.I18N.tr
import mods.eln.misc.*
import mods.eln.node.NodeBase
import mods.eln.node.NodePeriodicPublishProcess
import mods.eln.node.published
import mods.eln.node.six.*
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.ThermalLoad
import mods.eln.sim.mna.component.ResistorSwitch
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sim.process.destruct.VoltageStateWatchDog
import mods.eln.sim.process.destruct.WorldExplosion
import mods.eln.transparentnode.LampSupplyElement
import net.minecraft.client.gui.components.Button
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.opengl.GL11
import java.io.DataInputStream
import java.io.DataOutputStream

class EmergencyLampDescriptor(name: String, val cable: ElectricalCableDescriptor, val batteryCapacity: Double,
                              val chargePower: Double, val consumption: Double, val lightLevel: Int, model: Obj3D)
    : SixNodeDescriptor(name, EmergencyLampElement::class.java, EmergencyLampRender::class.java) {

    val mainCeiling: Obj3D.Obj3DPart = model.getPart("coreCeil")
    val panelCeiling: Obj3D.Obj3DPart = model.getPart("panelCeil")
    val lightCeiling: Obj3D.Obj3DPart = model.getPart("lightCeil")
    val mainWall: Obj3D.Obj3DPart = model.getPart("coreWall")
    val mainWallR: Obj3D.Obj3DPart = model.getPart("coreWallR")
    val mainWallL: Obj3D.Obj3DPart = model.getPart("coreWallL")
    val lightWall: Obj3D.Obj3DPart = model.getPart("lightWall")

    init {
        voltageLevelColor = VoltageLevelColor.fromCable(cable)
        // setDefaultIcon("emergencylamp")
    }

    override fun draw(poseStack: PoseStack, consumer: VertexConsumer, packedLight: Int, packedOverlay: Int, signal: Boolean) {
        mainCeiling.draw(poseStack, consumer, packedLight, packedOverlay)
        panelCeiling.draw(poseStack, consumer, packedLight, packedOverlay)
    }

    fun draw(poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, packedOverlay: Int, onCeiling: Boolean = false, on: Boolean = false, mirrorSign: Boolean = false) {
        if (onCeiling) {
            mainCeiling.draw(poseStack, buffer, packedLight, packedOverlay)

            if (on) {
                UtilsClient.drawLight(panelCeiling, poseStack, buffer, packedLight, packedOverlay)
                UtilsClient.drawLight(lightCeiling, poseStack, buffer, packedLight, packedOverlay, 0.3f, 0.3f, 0.3f, 1f)
            } else {
                panelCeiling.draw(poseStack, buffer, packedLight, packedOverlay)
            }
        } else {
            if (on) {
                UtilsClient.drawLight(mainWall, poseStack, buffer, packedLight, packedOverlay)
                UtilsClient.drawLight(if (mirrorSign) mainWallL else mainWallR, poseStack, buffer, packedLight, packedOverlay)
                UtilsClient.drawLight(lightWall, poseStack, buffer, packedLight, packedOverlay, 0.3f, 0.3f, 0.3f, 1f)
            } else {
                mainWall.draw(poseStack, buffer, packedLight, packedOverlay)
                (if (mirrorSign) mainWallL else mainWallR).draw(poseStack, buffer, packedLight, packedOverlay)
            }
        }
    }

    override fun getFrontFromPlace(side: Direction, player: Player)
        = super.getFrontFromPlace(side, player)!!.inverse()

    override fun appendHoverText(itemStack: ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<Component>, flag: TooltipFlag) {
        with(list) {
            add(Component.literal(tr("As long as power is provided, the internal battery")))
            add(Component.literal(tr("is charged and the lamp is off. On a power failure,")))
            add(Component.literal(tr("the lamp turns on and runs on batteries.")))
            add(Component.literal(Utils.plotVolt(tr("Nominal voltage:"), cable.electricalNominalVoltage)))
            add(Component.literal(Utils.plotEnergy(tr("Battery capacity:"), batteryCapacity)))
        }
    }
}

class EmergencyLampElement(_sixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor)
    : SixNodeElement(_sixNode, side, descriptor) {
// ...existing code...


    enum class Event(val value: Byte) {
        TOGGLE_POWERED_BY_CABLE(1),
        SET_CHANNEL(2)
    }

    val desc = descriptor as EmergencyLampDescriptor
    val load = NbtElectricalLoad("load")
    val chargingResistor = ResistorSwitch("chargingResistor", load, null)
    var on by published(false, {
        this.sixNode!!.lightValue = if (it) desc.lightLevel else 0
    })
    var charge = desc.batteryCapacity / 2
    var poweredByCable by published(false, {
        if (it) isConnectedToLampSupply = false
    }, triggerReconnect = true)
    var channel by published("Default channel")
    var isConnectedToLampSupply by published(false)

    val process = IProcess { deltaT ->
        if (!poweredByCable) {
            var closestPowerSupply: LampSupplyElement.PowerSupplyChannelHandle? = null
            var closestDistance = 10000f

            LampSupplyElement.channelMap[channel]?.forEach {
                val distance = it.element.node!!.coordinate.trueDistanceTo(this.sixNode!!.coordinate).toFloat()
                if (distance < closestDistance && distance <= it.element.range) {
                    closestDistance = distance
                    closestPowerSupply = it
                }
            }

            if (closestPowerSupply != null) {
                isConnectedToLampSupply = true
                if (closestPowerSupply!!.element.getChannelState(closestPowerSupply!!.id)) {
                    // closestPowerSupply!!.element.addToRp(chargingResistor.resistance)
                    load.state = closestPowerSupply!!.element.powerLoad.state
                } else {
                    load.state = 0.0
                }
            } else {
                isConnectedToLampSupply = false
                load.state = 0.0
            }
        }

        if (chargingResistor.voltage > 0.5 * desc.cable.electricalNominalVoltage) {
            on = false
            if (charge < desc.batteryCapacity) {
                chargingResistor.state = true
                charge = Math.min(charge + chargingResistor.power * deltaT, desc.batteryCapacity)
            } else {
                chargingResistor .state = false
            }
        } else {
            chargingResistor.state = false
            if (charge > 0) {
                on = true
                charge = Math.max(charge - desc.consumption * deltaT, 0.0)
            } else {
                on = false
            }
        }
    }

    override fun initialize() {
        chargingResistor.resistance =
            desc.cable.electricalNominalVoltage * desc.cable.electricalNominalVoltage / desc.chargePower
        desc.cable.applyTo(load)

        electricalLoadList.add(load)
        electricalComponentList.add(chargingResistor)
        slowProcessList.add(process)
        slowProcessList.add(NodePeriodicPublishProcess(this.sixNode!!, 2.0, 0.5))
        slowProcessList.add(VoltageStateWatchDog(load).setNominalVoltage(desc.cable.electricalNominalVoltage)
            .setDestroys(WorldExplosion(this).cableExplosion()))
    }

    override fun getConnectionMask(lrdu: LRDU) = when {
        poweredByCable && side == Direction.YP -> NodeBase.maskElectricalPower
        poweredByCable && (lrdu == front.left() || lrdu == front.right()) -> NodeBase.maskElectricalPower
        else -> 0
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad = load
    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? = null
    override fun multiMeterString() = buildString {
        append(Utils.plotVolt("U:", load.voltage))
        append(Utils.plotAmpere("I:", load.current))
        append(Utils.plotPercent("Charge:", charge / (sixNodeElementDescriptor as EmergencyLampDescriptor).batteryCapacity))
    }
    override fun thermoMeterString(): String = ""
    override fun getWaila() = mapOf(
        tr("State") to when {
            on -> tr("On")
            chargingResistor.state -> tr("Charging...")
            charge <= 0.0 -> tr("Batteries empty")
            else -> tr("Fully charged")
        },
        tr("Charge") to Utils.plotPercent("", charge / (sixNodeElementDescriptor as EmergencyLampDescriptor).batteryCapacity)
    )

    override fun networkSerialize(stream: DataOutputStream) {
        super.networkSerialize(stream)
        stream.writeFloat(charge.toFloat() / desc.batteryCapacity.toFloat())
        stream.writeBoolean(on)
        stream.writeBoolean(poweredByCable)
        stream.writeUTF(channel)
        stream.writeBoolean(isConnectedToLampSupply)
    }

    override fun networkUnserialize(stream: DataInputStream) {
        super.networkUnserialize(stream)
        when (stream.readByte()) {
            Event.TOGGLE_POWERED_BY_CABLE.value -> poweredByCable = !poweredByCable
            Event.SET_CHANNEL.value -> channel = stream.readUTF()
        }
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        on = nbt.getBoolean("on")
        charge = nbt.getDouble("charge")
        poweredByCable = nbt.getBoolean("poweredByCable")
        channel = nbt.getString("channel")
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        nbt.putBoolean("on", on)
        nbt.putDouble("charge", charge)
        nbt.putBoolean("poweredByCable", poweredByCable)
        nbt.putString("channel", channel)
    }

    override fun hasGui() = true
}

class EmergencyLampRender(entity: SixNodeEntity, side: Direction, descriptor: SixNodeDescriptor)
    : SixNodeElementRender(entity, side, descriptor) {

    val desc = descriptor as EmergencyLampDescriptor
    var charge = 0f
    var on = false
    var poweredByCable = false
    var channel = "Default channel"
    var isConnectedToLampSupply = false

    override fun draw() {
        super.draw()
        val poseStack = currentPoseStack ?: return
        val buffer = currentBuffer ?: return
        val light = currentLight
        val overlay = currentOverlay
        
        front!!.rotatePoseOnX(poseStack)
        desc.draw(poseStack, buffer, light, overlay, side == Direction.YP, on, front == LRDU.Up)
    }

    override fun publishUnserialize(stream: DataInputStream) {
        super.publishUnserialize(stream)
        charge = stream.readFloat()
        on = stream.readBoolean()
        poweredByCable = stream.readBoolean()
        channel = stream.readUTF()
        isConnectedToLampSupply = stream.readBoolean()
    }

    override fun newGuiDraw(side: Direction, player: Player) = EmergencyLampGui(this)

    override fun getCableRender(lrdu: LRDU): CableRenderDescriptor? = if (poweredByCable) when {
        side == Direction.YP -> desc.cable.render
        lrdu == front!!.left() || lrdu == front!!.right() -> desc.cable.render
        else -> null
    } else null
}

class EmergencyLampGui(private var render: EmergencyLampRender)
    : ScreenEln() {
    private lateinit var buttonSupplyType: Button
    private lateinit var channel: GuiTextFieldEln
    private lateinit var charge: GuiVerticalProgressBar

    override fun initGui() {
        super.initGui()
        buttonSupplyType = newGuiButton(18, 12, 140, "") {
            render.clientSend(EmergencyLampElement.Event.TOGGLE_POWERED_BY_CABLE.value.toInt())
        }
        channel = newGuiTextField(19, 38, 138)
        channel.setComment(arrayOf(tr("Specify the supply channel")))
        channel.text = render.channel
        channel.observer = object : GuiTextFieldEln.GuiTextFieldElnObserver {
            override fun textFieldNewValue(textField: GuiTextFieldEln, value: String) {
                render.clientSetString(EmergencyLampElement.Event.SET_CHANNEL.value, value)
            }
        }
        charge = newGuiVerticalProgressBar(166, 12, 16, 39)
        charge.setColor(0.2f, 0.5f, 0.8f)
    }

    /*
    override fun guiObjectEvent(guiObject: IGuiObject) {
        // super.guiObjectEvent(guiObject)
    }
    */

    override fun newHelper(): GuiHelperContainer = GuiHelperContainer(this, 196, 64, 8, 84)

    override fun preDraw(guiGraphics: GuiGraphics, f: Float, x: Int, y: Int) {
        super.preDraw(guiGraphics, f, x, y)

        if (!render.poweredByCable) {
            buttonSupplyType.message = Component.literal(tr("Powered by Lamp Supply"))
            channel.visible = true
            if (render.isConnectedToLampSupply)
                channel.setComment(arrayOf(tr("Specify the supply channel"), "§2" + tr("connected to " + render.channel)))
            else
                channel.setComment(arrayOf(tr("Specify the supply channel"), "§4" + tr("%1$ is not in range!", render.channel)))
        } else {
            channel.visible = false
            buttonSupplyType.message = Component.literal(tr("Powered by cable"))
        }
        charge.value = render.charge
        charge.setComment(0, Utils.plotPercent("Charge: ", render.charge.toDouble()))
    }
}

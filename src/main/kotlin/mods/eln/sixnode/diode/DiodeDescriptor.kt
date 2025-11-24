package mods.eln.sixnode.diode

import mods.eln.misc.IFunction
import mods.eln.misc.Obj3D
import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.misc.RealisticEnum
import mods.eln.misc.VoltageLevelColor
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.ThermalLoadInitializer
import mods.eln.sim.mna.component.ResistorSwitch
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL11
import java.util.Collections
import mods.eln.i18n.I18N.tr
// import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraft.world.level.Level
import net.minecraft.world.item.TooltipFlag
import net.minecraft.network.chat.Component
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer

class DiodeDescriptor(
    name: String,
    var IfU: IFunction,
    Imax: Double,
    var stdU: Double,
    var stdI: Double,
    @JvmField var thermal: ThermalLoadInitializer,
    @JvmField var cable: ElectricalCableDescriptor,
    obj: Obj3D
) : SixNodeDescriptor(name, DiodeElement::class.java, DiodeRender::class.java) {

    private val base: Obj3DPart? = obj.getPart("Base")
    private val diodeCables: Obj3DPart? = obj.getPart("DiodeCables")
    private val diodeCore: Obj3DPart? = obj.getPart("DiodeCore")

    var descriptor: String? = null

    override fun draw(poseStack: PoseStack, consumer: VertexConsumer, packedLight: Int, packedOverlay: Int, signal: Boolean) {
        base?.draw(poseStack, consumer, packedLight, packedOverlay)
        diodeCables?.draw(poseStack, consumer, packedLight, packedOverlay)
        diodeCore?.draw(poseStack, consumer, packedLight, packedOverlay)
    }

    init {
        thermal.setMaximalPower(stdU * stdI * 1.2)
        
        if (cable.signalWire) {
            voltageLevelColor = VoltageLevelColor.SignalVoltage
        } else {
            voltageLevelColor = VoltageLevelColor.Neutral
        }
    }

    override fun setParent(item: Item, damage: Int) {
        super.setParent(item, damage)
        Data.addEnergy(newItemStack())
    }

    /*
    override fun shouldUseRenderHelper(type: ItemRenderType, item: ItemStack, helper: ItemRendererHelper): Boolean {
        return type != ItemRenderType.INVENTORY
    }

    override fun handleRenderType(item: ItemStack, type: ItemRenderType): Boolean {
        return true
    }

    override fun shouldUseRenderHelperEln(type: ItemRenderType, item: ItemStack, helper: ItemRendererHelper): Boolean {
        return type != ItemRenderType.INVENTORY
    }

    override fun renderItem(type: ItemRenderType, item: ItemStack, vararg data: Any) {
        if (type == ItemRenderType.INVENTORY) {
            super.renderItem(type, item, *data)
        } else {
            GL11.glTranslatef(0.0f, 0.0f, -0.2f)
            GL11.glScalef(1.25f, 1.25f, 1.25f)
            GL11.glRotatef(-90.f, 0.f, 1.f, 0.f)
            draw()
        }
    }
    */

    fun applyTo(load: ThermalLoad) {
        thermal.applyTo(load)
    }

    fun applyTo(load: ElectricalLoad) {
        cable.applyTo(load)
    }

    fun applyTo(resistorSwitch: ResistorSwitch) {
        resistorSwitch.setResistance(stdU / stdI)
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, level, tooltip, flag)
        for (s in tr("Electrical current can only\nflow through the diode\nfrom anode to cathode").split("\n")) {
            tooltip.add(Component.literal(s))
        }
    }

    override fun addRealismContext(list: MutableList<String>): RealisticEnum {
        super.addRealismContext(list)
        list.add(tr("Works, with the caveat that it's delayed a sim tick"))
        return RealisticEnum.IDEAL
    }

    fun draw() {
        base?.draw()
        diodeCables?.draw()
        diodeCore?.draw()
    }
}
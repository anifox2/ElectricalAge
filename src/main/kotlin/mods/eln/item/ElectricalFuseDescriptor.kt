package mods.eln.item

import mods.eln.misc.Obj3D
import mods.eln.misc.VoltageLevelColor
import mods.eln.misc.preserveMatrix
import mods.eln.sixnode.electricalcable.ElectricalCableDescriptor
import mods.eln.wiki.Data
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
// import net.minecraftforge.client.IItemRenderer
import org.lwjgl.opengl.GL11

class ElectricalFuseDescriptor(name: String, val cableDescriptor: ElectricalCableDescriptor?, obj: Obj3D?) :
    GenericItemUsingDamageDescriptorUpgrade(name) {

    companion object {
        var BlownFuse: ElectricalFuseDescriptor? = null
    }

    private val fuseType = obj?.getPart("FuseType")
    private val fuseOk = obj?.getPart("FuseOk")
    private val fuse = obj?.getPart("Fuse")

    init {
        if (cableDescriptor != null) {
            setDefaultIcon("electricalfuse")
            // voltageLevelColor = VoltageLevelColor.fromCable(cableDescriptor)
        } else {
            setDefaultIcon("blownelectricalfuse")
            // voltageLevelColor = VoltageLevelColor.Neutral
        }
    }

    /* Rendering disabled
    override fun shouldUseRenderHelper(type: IItemRenderer.ItemRenderType?, item: ItemStack?,
                                       helper: IItemRenderer.ItemRendererHelper?) = type != IItemRenderer.ItemRenderType.INVENTORY

    override fun renderItem(type: IItemRenderer.ItemRenderType?, item: ItemStack?, vararg data: Any?) {
       // ...
    }
    */

    override fun setParent(item: Any?, damage: Int) {
        super.setParent(item, damage)
        Data.addWiring(newItemStack())
    }
}

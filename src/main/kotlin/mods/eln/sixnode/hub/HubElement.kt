package mods.eln.sixnode.hub

import mods.eln.Eln
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.Utils
import mods.eln.node.NodeBase
import mods.eln.node.six.SixNode
import mods.eln.node.six.SixNodeDescriptor
import mods.eln.node.six.SixNodeElement
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.ThermalLoad
import mods.eln.sim.nbt.NbtElectricalLoad
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.Container
import net.minecraft.nbt.CompoundTag

class HubElement(sixNode: SixNode, side: Direction, descriptor: SixNodeDescriptor) : SixNodeElement(sixNode, side, descriptor) {

    var electricalLoad = NbtElectricalLoad("electricalLoad")

    init {
        electricalLoadList.add(electricalLoad)
    }

    override fun getElectricalLoad(lrdu: LRDU, mask: Int): ElectricalLoad? {
        return electricalLoad
    }

    override fun getThermalLoad(lrdu: LRDU, mask: Int): ThermalLoad? {
        return null
    }

    override fun getConnectionMask(lrdu: LRDU): Int {
        return NodeBase.maskElectricalPower
    }

    override fun multiMeterString(): String {
        return Utils.plotVolt("U", electricalLoad.voltage)
    }

    override fun thermoMeterString(): String {
        return ""
    }

    override fun readFromNBT(nbt: CompoundTag) {
        super.readFromNBT(nbt)
        if (nbt.contains("electricalLoad")) {
            electricalLoad.readFromNBT(nbt, "electricalLoad")
        }
    }

    override fun writeToNBT(nbt: CompoundTag) {
        super.writeToNBT(nbt)
        electricalLoad.writeToNBT(nbt, "electricalLoad")
    }

    override fun initialize() {
        Eln.applySmallRs(electricalLoad)
    }

    override val inventory: Container?
        get() = null

    override fun hasGui(): Boolean {
        return false
    }

    override fun newContainer(side: Direction, player: Player): AbstractContainerMenu? {
        return null
    }

    override fun onBlockActivated(entityPlayer: Player, side: Direction, vx: Float, vy: Float, vz: Float): Boolean {
        return false
    }
}

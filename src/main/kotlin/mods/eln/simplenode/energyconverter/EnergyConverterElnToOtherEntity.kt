package mods.eln.simplenode.energyconverter

import mods.eln.Other
import mods.eln.misc.Direction
import mods.eln.node.simple.SimpleNodeEntity
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.Direction as MCDirection
import java.io.DataInputStream
import java.io.IOException
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

class EnergyConverterElnToOtherEntity(pos: BlockPos, state: BlockState) : SimpleNodeEntity(mods.eln.init.Registration.ENERGY_CONVERTER_BLOCK_ENTITY.get(), "ElnToOther", pos, state) {
    @JvmField
    var selectedResistance = 0.0
    @JvmField
    var hasChanges = false
    // var ocEnergy: EnergyConverterElnToOtherFireWallOc? = null
    @JvmField
    var addedToEnet = false
    var ic2tier = 1

    @SideOnly(Side.CLIENT)
    override fun newGuiDraw(side: Direction, player: Player): Screen {
        return EnergyConverterElnToOtherGui(this)
    }

    override fun serverPublishUnserialize(stream: DataInputStream) {
        super.serverPublishUnserialize(stream)
        try {
            selectedResistance = stream.readDouble()
            ic2tier = stream.readInt()
            hasChanges = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /*
    // ********************IC2********************
    override fun emitsEnergyTo(receiver: BlockEntity?, direction: MCDirection?): Boolean {
        if (level!!.isClientSide) return false
        node ?: return false
        return true
    }

    override fun getOfferedEnergy(): Double {
        if (level!!.isClientSide) return 0.0
        if (node == null) return 0.0
        val node = node as EnergyConverterElnToOtherNode
        return node.availableEnergyInModUnitsWithLimit(IC2Tiers.values().first { it.tier == node.ic2tier }.euPerTick.toDouble(), Other.getWattsToEu())
    }

    override fun drawEnergy(amount: Double) {
        if (level!!.isClientSide) return
        if (node == null) return
        val node = node as EnergyConverterElnToOtherNode
        node.drawEnergy(amount, Other.getWattsToEu())
    }

    override fun getSourceTier(): Int {
        return 5
    }

    // ***************** OC **********************
    fun getOc(): EnergyConverterElnToOtherFireWallOc {
        if (ocEnergy == null) ocEnergy = EnergyConverterElnToOtherFireWallOc(this)
        return ocEnergy!!
    }

    override fun node(): Node? {
        return getOc().node
    }

    override fun onConnect(node: Node) {
    }

    override fun onDisconnect(node: Node) {
    }

    override fun onMessage(message: Message) {
    }

    // ***************** RF **********************
    override fun receiveEnergy(from: MCDirection?, maxReceive: Int, simulate: Boolean): Int {
        return 0
    }

    override fun extractEnergy(from: MCDirection?, maxExtract: Int, simulate: Boolean): Int {
        return 0
    }

    override fun getEnergyStored(from: MCDirection?): Int {
        return 0
    }

    override fun getMaxEnergyStored(from: MCDirection?): Int {
        return 0
    }

    override fun canConnectEnergy(from: MCDirection?): Boolean {
        return true
    }
    */

    // ***************** Common ******************
    
    fun updateEntity() {
        // if (Other.isIc2Loaded()) EnergyConverterElnToOtherFireWallIc2.updateEntity(this)
        // if (Other.isOcLoaded()) getOc().updateEntity()
        // if (Other.isTeLoaded()) EnergyConverterElnToOtherFireWallRf.updateEntity(this)
    }

    override fun setRemoved() {
        super.setRemoved()
        /*
        if (Other.isIc2Loaded()) {
            EnergyConverterElnToOtherFireWallIc2.invalidate(this)
            EnergyConverterElnToOtherFireWallIc2.onChunkUnload(this)
        }
        if (Other.isOcLoaded()) {
            getOc().invalidate()
            getOc().onChunkUnload()
        }
        */
    }

    override fun load(nbt: CompoundTag) {
        super.load(nbt)
        // if (Other.isOcLoaded()) getOc().load(nbt)
    }

    override fun saveAdditional(nbt: CompoundTag) {
        super.saveAdditional(nbt)
        // if (Other.isOcLoaded()) getOc().save(nbt)
    }
    
    override fun onLoad() {
        super.onLoad()
        // if (Other.isOcLoaded()) getOc().constructor()
        // connect()
    }
}


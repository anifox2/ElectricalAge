package mods.eln.transparentnode.turret

import mods.eln.item.IConfigurable
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.SlewLimiter
import mods.eln.misc.Utils
import mods.eln.node.NodePeriodicPublishProcess
import mods.eln.sim.ElectricalLoad
import mods.eln.sim.IProcess
import mods.eln.sim.nbt.NbtElectricalLoad
import mods.eln.sim.nbt.NbtResistor
import mods.eln.node.transparent.TransparentNodeDescriptor
import mods.eln.node.transparent.TransparentNodeElement
import mods.eln.node.transparent.TransparentNode
import mods.eln.node.transparent.TransparentNodeElementInventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

class TurretElement(
    node: TransparentNode?,
    descriptor: TransparentNodeDescriptor
) : TransparentNodeElement(node, descriptor), IConfigurable {

    override val descriptor: TurretDescriptor get() = transparentNodeDescriptor as TurretDescriptor
    private val simulation = TurretMechanicsSimulation(this.descriptor)
    private val powerLoad = NbtElectricalLoad("powerLoad")
    private val powerResistor = NbtResistor("powerResistor", powerLoad, null)
    
    var chargePower: Double = 0.0
    var filterIsSpare = false

    override var inventory: Container? = null

    override fun initialize() {
    }

    init {
        inventory = TransparentNodeElementInventory(1, 64, this)
        chargePower = this.descriptor.properties.chargePower
        electricalLoadList.add(powerLoad)
        electricalComponentList.add(powerResistor)
        slowProcessList.add(simulation)
        if (node != null) {
            slowProcessList.add(NodePeriodicPublishProcess(node, 0.1, 0.1))
        }
    }

    override fun getElectricalLoad(side: Direction, lrdu: LRDU): ElectricalLoad? {
        if (lrdu == LRDU.Down) return powerLoad
        return null
    }

    override fun readConfigTool(compound: CompoundTag, invoker: Player) {
        // Configuration logic
    }

    override fun writeConfigTool(compound: CompoundTag, invoker: Player) {
        // Configuration logic
    }

    override fun hasGui(): Boolean {
        return false
    }

    class TurretMechanicsSimulation(val descriptor: TurretDescriptor) : IProcess {
        private val turretAngle = SlewLimiter(descriptor.properties.turretAimAnimationSpeed)
        private val gunPosition = SlewLimiter(descriptor.properties.gunArmAnimationSpeed, descriptor.properties.gunDisarmAnimationSpeed)
        private val gunElevation = SlewLimiter(descriptor.properties.gunAimAnimationSpeed)
        private var shootDuration = 0f
        private var enabled = false

        override fun process(time: Double) {
            turretAngle.step(time.toFloat())
            gunPosition.step(time.toFloat())
            gunElevation.step(time.toFloat())
            
            if (shootDuration > 0) {
                shootDuration -= time.toFloat()
            }
        }
        
        fun getTurretAngle() = turretAngle.position
        fun getGunElevation() = gunElevation.position
        fun getGunPosition() = gunPosition.position
    }
}

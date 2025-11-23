package mods.eln.item.electricalitem

import mods.eln.i18n.I18N.tr
import mods.eln.item.electricalinterface.IItemEnergyBattery
import mods.eln.misc.Utils
//import mods.eln.misc.UtilsClient
import mods.eln.wiki.Data
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionHand
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import mods.eln.misc.nbt
import mods.eln.misc.getDouble
import mods.eln.misc.putDouble
import mods.eln.misc.getInt
import mods.eln.misc.setInt
import mods.eln.misc.putBoolean

class ElectricalLampItem(name: String, var lightMin: Int, var rangeMin: Int, dischargeMin: Double, var lightMax: Int,
                         rangeMax: Int, dischargeMax: Double, energyStorage: Double, chargePower: Double) : LampItem(name), IItemEnergyBattery {
    var rangeMax: Int
    var energyStorage: Double
    var dischargeMin: Double
    var dischargeMax: Double
    var chargePower: Double
    var on: ResourceLocation
    var off: ResourceLocation
    var boosted: ResourceLocation
    override fun setParent(item: Any?, damage: Int) {
        super.setParent(item, damage)
        //Data.addPortable(newItemStack())
        //Data.addLight(newItemStack())
    }

    override fun getRange(stack: ItemStack): Int {
        return if (getLightState(stack) == 1) rangeMin else rangeMax
    }

    override fun getLight(stack: ItemStack): Int {
        val energy = getEnergy(stack)
        val state = getLightState(stack)
        var power = 0.0
        when (state) {
            1 -> power = dischargeMin
            2 -> power = dischargeMax
        }
        return if (energy > power) {
            //setEnergy(stack, energy - power);
            getLightLevel(stack)
        } else {
            //setEnergy(stack,0);
            0
        }
    }

    override fun getDefaultNBT(): CompoundTag? {
        val nbt = CompoundTag()
        nbt.putDouble("energy", 0.0)
        nbt.putBoolean("powerOn", false)
        nbt.putInt("rand", (Math.random() * 0xFFFFFFF).toInt())
        return nbt
    }

    public override fun getLightState(stack: ItemStack): Int {
        return stack.getInt("LightState")
    }

    fun setLightState(stack: ItemStack, value: Int) {
        stack.setInt("LightState", value)
    }

    fun getLightLevel(stack: ItemStack): Int {
        return if (getLightState(stack) == 1) lightMin else lightMax
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        if (!level.isClientSide && getEnergy(stack) > 0) {
            var lightState = getLightState(stack) + 1
            if (lightState > 2) lightState = 0
            when (lightState) {
                0 -> player.sendSystemMessage(Component.literal("Flashlight OFF"))
                1 -> player.sendSystemMessage(Component.literal("Flashlight ON"))
                2 -> player.sendSystemMessage(Component.literal("Flashlight BOOSTED"))
                else -> {
                }
            }
            setLightState(stack, lightState)
        }
        return InteractionResultHolder.success(stack)
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(stack, level, list, flag)
        list.add(Component.literal(tr("Discharge power: %1\$W", Utils.plotValue(dischargeMin))))
        
        list.add(Component.literal(tr("Stored Energy: %1\$J (%2$%)", Utils.plotValue(getEnergy(stack)),
            (getEnergy(stack) / energyStorage * 100).toInt())))
        list.add(Component.literal(tr("State:") + " " + if (getLightState(stack) != 0) tr("On") else tr("Off")))
    }


    /*
	@Override
	public double putEnergy(ItemStack stack, double energy, double time) {
		double hit = Math.min(energy,Math.min(energyStorage - getEnergy(stack), chargePower * time));
		setEnergy(stack, getEnergy(stack) + hit);
		return energy - hit;
	}

	@Override
	public boolean isFull(ItemStack stack) {
		return getEnergy(stack) == energyStorage;
	}
*/
    override fun getEnergy(stack: ItemStack): Double {
        return stack.getDouble("energy")
    }

    override fun setEnergy(stack: ItemStack, value: Double) {
        stack.putDouble("energy", value)
    }

    override fun getEnergyMax(stack: ItemStack): Double {
        return energyStorage
    }

    override fun getTransferRate(stack: ItemStack): Double {
        return chargePower
    }

    override fun getChargePower(stack: ItemStack): Double {
        return chargePower
    }

    fun getDischagePower(stack: ItemStack): Double {
        return 0.0
    }

    fun getPriority(stack: ItemStack): Int {
        return 0
    }

    fun electricalItemUpdate(stack: ItemStack, time: Double) {
        val energy = getEnergy(stack)
        val state = getLightState(stack)
        var power = 0.0
        when (state) {
            1 -> power = dischargeMin * time
            2 -> power = dischargeMax * time
        }
        if (energy > power) {
			setEnergy(stack, energy - power)
		} else {
			setEnergy(stack, 0.0)
			setLightState(stack, 0)
		}
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        super.inventoryTick(stack, level, entity, slotId, isSelected)
        if (!level.isClientSide) {
            electricalItemUpdate(stack, 0.05)
        }
    }

    init {
        this.rangeMax = rangeMax + 1 //adding 1 is a hack. Since the value is locked at 1 anyway, I would rather not change a ton of code to make this work, and just double its range by adding 1 here
        this.chargePower = chargePower
        this.dischargeMin = dischargeMin
        this.dischargeMax = dischargeMax
        this.energyStorage = energyStorage
        //setDefaultIcon(name + "off")
        boosted = ResourceLocation("eln", "textures/items/" + name.replace(" ", "").lowercase() + "boosted.png")
        on = ResourceLocation("eln", "textures/items/" + name.replace(" ", "").lowercase() + "on.png")
        off = ResourceLocation("eln", "textures/items/" + name.replace(" ", "").lowercase() + "off.png")
        //	off = new ResourceLocation("eln", "/model/StoneFurnace/all.png");
    }
}

package mods.eln.item.electricalitem

import mods.eln.i18n.I18N.tr
import mods.eln.misc.Utils
import mods.eln.sim.IProcess
import mods.eln.wiki.Data
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.tags.BlockTags
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionHand
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.min

class ElectricalAxe(name: String, strengthOn: Float, strengthOff: Float,
                    energyStorage: Double, energyPerBlock: Double, chargePower: Double)
    : ElectricalTool(name, strengthOn, strengthOff, energyStorage, energyPerBlock, chargePower) {

    /*
    override fun setParent(item: Item?, damage: Int) {
        super.setParent(item, damage)
        //Data.addPortable(newItemStack())
    }
    */

    override fun appendHoverText(itemStack: net.minecraft.world.item.ItemStack, level: net.minecraft.world.level.Level?, list: MutableList<net.minecraft.network.chat.Component>, flag: net.minecraft.world.item.TooltipFlag) {
        super.appendHoverText(itemStack, level, list, flag)
        list.add(net.minecraft.network.chat.Component.literal(tr("Cuts down trees. Right-click to make it act like a regular axe.")))
    }

    override fun getDestroySpeed(stack: ItemStack, state: BlockState): Float {
        return if (state.`is`(BlockTags.LOGS) || state.`is`(BlockTags.LEAVES) || state.`is`(BlockTags.PLANKS) || state.`is`(BlockTags.WART_BLOCKS)) {
            getStrength(stack)
        } else {
            super.getDestroySpeed(stack, state)
        }
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(hand)
        if (!level.isClientSide) {
            setCapitation(player, stack, !getCapitation(stack))
        }
        return InteractionResultHolder.success(stack)
    }

    private fun getCapitation(stack: ItemStack): Boolean {
        val nbt = stack.orCreateTag
        if (!nbt.contains("capitation")) {
            nbt.putBoolean("capitation", true)
        }
        return nbt.getBoolean("capitation")
    }

    private fun setCapitation(p: Player?, stack: ItemStack, capitation: Boolean) {
        stack.orCreateTag.putBoolean("capitation", capitation)
        if (p != null) {
            Utils.addChatMessage(p, "Set treecapitation to $capitation")
        }
    }

    override fun mineBlock(stack: ItemStack, level: Level, state: BlockState, pos: BlockPos, entity: LivingEntity): Boolean {
        return if (entity is Player && getCapitation(stack) && !level.isClientSide) {
            TreeCapitation.addBlockSwapper(
                level = level,
                player = entity,
                tool = this,
                stack = stack,
                leaves = true,
                origCoords = pos
            )
            true
        } else {
            super.mineBlock(stack, level, state, pos, entity)
        }
    }

    companion object {
        const val BLOCK_RANGE = 32
        const val LEAF_BLOCK_RANGE = 4
        const val BLOCK_SWAP_RATE = 16
        const val SINGLE_BLOCK_RADIUS = 1
    }
}

object TreeCapitation : IProcess {
    const val BLOCK_SWAP_RATE = 10
    const val BLOCK_RANGE = 32
    const val LEAF_BLOCK_RANGE = 3
    const val SINGLE_BLOCK_RADIUS = 1

    private val blockSwappers: MutableMap<String, List<BlockSwapper>> = HashMap()

    override fun process(time: Double) {
        for ((dim, swappers) in blockSwappers) {
            blockSwappers[dim] = swappers.filter { it.tick() }
        }
    }

    fun addBlockSwapper(level: Level, player: Player, tool: ElectricalTool, origCoords: BlockPos, leaves: Boolean, stack: ItemStack) {
        if (level.isClientSide) return

        val swapper = BlockSwapper(level, player, tool, origCoords, BLOCK_RANGE, leaves, stack)
        val dim = level.dimension().location().toString()
        blockSwappers[dim] = blockSwappers[dim]?.plus(swapper) ?: listOf(swapper)
    }

    private class BlockSwapper(
        private val level: Level,
        private val player: Player,
        private val tool: ElectricalTool,
        private val origin: BlockPos,
        private val range: Int,
        private val treatLeavesSpecial: Boolean,
        val stack: ItemStack) {

        private val candidateQueue: PriorityQueue<SwapCandidate>
        private val completedCoords: MutableSet<BlockPos>

        init {
            this.candidateQueue = PriorityQueue<SwapCandidate>()
            this.completedCoords = HashSet()
            candidateQueue.offer(SwapCandidate(this.origin, this.range))
        }

        fun tick(): Boolean {
            if (candidateQueue.isEmpty()) return false

            var remainingSwaps = ElectricalAxe.BLOCK_SWAP_RATE
            while (remainingSwaps > 0 && !candidateQueue.isEmpty()) {
                val candidate = candidateQueue.poll()

                if (completedCoords.contains(candidate.coordinates)) continue
                if (candidate.range <= 0) continue

                removeBlockWithDrops(
                    player = player,
                    tool = tool,
                    stack = stack,
                    level = level,
                    pos = candidate.coordinates
                )

                remainingSwaps--
                completedCoords.add(candidate.coordinates)

                for (adj in adjacent(candidate.coordinates)) {
                    val state = level.getBlockState(adj)
                    val isWood = state.`is`(net.minecraft.tags.BlockTags.LOGS)
                    val isLeaf = state.`is`(net.minecraft.tags.BlockTags.LEAVES)

                    if (!isWood && !isLeaf) continue

                    val newRange = candidate.range - 1

                    candidateQueue.offer(SwapCandidate(adj, newRange))
                }
            }
            return true
        }

        fun adjacent(original: BlockPos): List<BlockPos> {
            val coords = ArrayList<BlockPos>()
            for (dx in -ElectricalAxe.SINGLE_BLOCK_RADIUS..ElectricalAxe.SINGLE_BLOCK_RADIUS)
                for (dy in -ElectricalAxe.SINGLE_BLOCK_RADIUS..ElectricalAxe.SINGLE_BLOCK_RADIUS)
                    for (dz in -ElectricalAxe.SINGLE_BLOCK_RADIUS..ElectricalAxe.SINGLE_BLOCK_RADIUS) {
                        if (dx == 0 && dy == 0 && dz == 0) continue
                        coords.add(original.offset(dx, dy, dz))
                    }
            return coords
        }

        class SwapCandidate(var coordinates: BlockPos, var range: Int) : Comparable<SwapCandidate> {
            override fun compareTo(other: SwapCandidate): Int {
                return other.range - range
            }
            override fun equals(other: Any?): Boolean {
                if (other !is SwapCandidate) return false
                return coordinates == other.coordinates && range == other.range
            }
            override fun hashCode(): Int {
                var result = coordinates.hashCode()
                result = 31 * result + range
                return result
            }
        }
    }

    fun removeBlockWithDrops(player: Player, tool: ElectricalTool, stack: ItemStack, level: Level, pos: BlockPos) {
        if (level.isClientSide || !level.isLoaded(pos)) return

        val state = level.getBlockState(pos)
        if (!state.isAir && state.getDestroySpeed(level, pos) >= 0) {
            if (!player.abilities.instabuild) {
                tool.subtractEnergyForBlockBreak(stack, state)
                level.destroyBlock(pos, true)
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
            }
        }
    }
}
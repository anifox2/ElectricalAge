package mods.eln.misc

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.server.ServerLifecycleHooks
import kotlin.math.abs
import net.minecraft.util.AABB
import net.minecraft.util.Direction
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.api.distmarker.Dist
import net.minecraft.client.Minecraft

class Coordinate : INBTTReady {
    @JvmField
    var x = 0
    @JvmField
    var y = 0
    @JvmField
    var z = 0
    @JvmField
    var dimension = 0

    constructor() {
        x = 0
        y = 0
        z = 0
        dimension = 0
    }

    constructor(x: Int, y: Int, z: Int, dimension: Int) {
        this.x = x
        this.y = y
        this.z = z
        this.dimension = dimension
    }
    
    constructor(pos: BlockPos, dimension: Int) {
        this.x = pos.x
        this.y = pos.y
        this.z = pos.z
        this.dimension = dimension
    }

    constructor(nbt: CompoundTag, str: String) {
        readFromNBT(nbt, str)
    }

    constructor(tileEntity: BlockEntity) {
        this.x = tileEntity.blockPos.x
        this.y = tileEntity.blockPos.y
        this.z = tileEntity.blockPos.z
        this.dimension = 0 // TODO: Fix dimension mapping
    }

    constructor(other: Coordinate) {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        this.dimension = other.dimension
    }

    override fun readFromNBT(nbt: CompoundTag, str: String) {
        x = nbt.getInt(str + "x")
        y = nbt.getInt(str + "y")
        z = nbt.getInt(str + "z")
        dimension = nbt.getInt(str + "d")
    }

    override fun writeToNBT(nbt: CompoundTag, str: String) {
        nbt.putInt(str + "x", x)
        nbt.putInt(str + "y", y)
        nbt.putInt(str + "z", z)
        nbt.putInt(str + "d", dimension)
    }

    override fun toString(): String {
        return "X : $x Y : $y Z : $z D : $dimension"
    }
    
    fun toBlockPos(): BlockPos {
        return BlockPos(x, y, z)
    }
    
    override fun equals(other: Any?): Boolean {
        if (other !is Coordinate) return false
        return other.x == x && other.y == y && other.z == z && other.dimension == dimension
    }
    
    override fun hashCode(): Int {
        return (x + y) * 0x10101010 + z
    }

    fun move(dir: Direction) {
        val mcDir = dir.toMCDirection()
        x += mcDir.stepX
        y += mcDir.stepY
        z += mcDir.stepZ
    }

    fun moved(dir: Direction): Coordinate {
        val c = Coordinate(this)
        c.move(dir)
        return c
    }

    fun trueDistanceTo(other: Coordinate): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble())
    }

    fun getAABB(range: Double): net.minecraft.util.AABB {
        return net.minecraft.util.AABB(x.toDouble() - range, y.toDouble() - range, z.toDouble() - range, x.toDouble() + 1 + range, y.toDouble() + 1 + range, z.toDouble() + 1 + range)
    }

    fun getAABB(range: Int): net.minecraft.util.AABB {
        return getAABB(range.toDouble())
    }

    fun getBlock(world: Level): net.minecraft.world.level.block.Block {
        return world.getBlockState(BlockPos(x, y, z)).block
    }

    fun getBlock(world: Level, dx: Int, dy: Int, dz: Int): net.minecraft.world.level.block.Block {
        return world.getBlockState(BlockPos(x + dx, y + dy, z + dz)).block
    }

    fun world(): Level {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return Minecraft.getInstance().level!!
        } else {
            val server = ServerLifecycleHooks.getCurrentServer()
            // TODO: Map dimension properly. For now defaulting to Overworld.
            return server.getLevel(Level.OVERWORLD)!!
        }
    }
}

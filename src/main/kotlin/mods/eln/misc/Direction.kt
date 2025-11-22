package mods.eln.misc

import net.minecraft.core.Direction as MCDirection
import org.lwjgl.opengl.GL11
import net.minecraft.world.phys.Vec3

enum class Direction(val int: Int) {
    XN(0), // West
    XP(1), // East
    YN(2), // Down
    YP(3), // Up
    ZN(4), // North
    ZP(5); // South

    fun rotateFromXN(v: Vec3): Vec3 {
        return when (this) {
            XN -> v
            XP -> Vec3(-v.x, v.y, -v.z)
            YN -> Vec3(-v.y, v.x, v.z)
            YP -> Vec3(v.y, -v.x, v.z)
            ZN -> Vec3(v.z, v.y, -v.x)
            ZP -> Vec3(-v.z, v.y, v.x)
        }
    }

    fun rotateOnXnLeft(v: Vec3): Vec3 {
        return when (this) {
            YP -> Vec3(v.x, -v.z, v.y)
            YN -> Vec3(v.x, v.z, -v.y)
            ZP -> Vec3(v.x, -v.y, -v.z)
            else -> v
        }
    }

    fun toMCDirection(): MCDirection {
        return when (this) {
            XN -> MCDirection.WEST
            XP -> MCDirection.EAST
            YN -> MCDirection.DOWN
            YP -> MCDirection.UP
            ZN -> MCDirection.NORTH
            ZP -> MCDirection.SOUTH
        }
    }

    fun glRotateXnRef() {
        when (this) {
            XN -> {}
            XP -> GL11.glRotatef(180f, 0f, 1f, 0f)
            YN -> GL11.glRotatef(90f, 0f, 0f, 1f)
            YP -> GL11.glRotatef(-90f, 0f, 0f, 1f)
            ZN -> GL11.glRotatef(-90f, 0f, 1f, 0f)
            ZP -> GL11.glRotatef(90f, 0f, 1f, 0f)
        }
    }

    fun glRotateZnRef() {
        when (this) {
            ZN -> {}
            ZP -> GL11.glRotatef(180f, 0f, 1f, 0f)
            YN -> GL11.glRotatef(-90f, 1f, 0f, 0f)
            YP -> GL11.glRotatef(90f, 1f, 0f, 0f)
            XN -> GL11.glRotatef(90f, 0f, 1f, 0f)
            XP -> GL11.glRotatef(-90f, 0f, 1f, 0f)
        }
    }

    fun glRotateZnRefInv() {
        when (this) {
            ZN -> {}
            ZP -> GL11.glRotatef(180f, 0f, 1f, 0f)
            YN -> GL11.glRotatef(90f, 1f, 0f, 0f)
            YP -> GL11.glRotatef(-90f, 1f, 0f, 0f)
            XN -> GL11.glRotatef(-90f, 0f, 1f, 0f)
            XP -> GL11.glRotatef(90f, 0f, 1f, 0f)
        }
    }

    companion object {
        fun fromMCDirection(dir: MCDirection): Direction {
            return when (dir) {
                MCDirection.WEST -> XN
                MCDirection.EAST -> XP
                MCDirection.DOWN -> YN
                MCDirection.UP -> YP
                MCDirection.NORTH -> ZN
                MCDirection.SOUTH -> ZP
            }
        }
        
        fun fromInt(i: Int): Direction {
            return values().first { it.int == i }
        }
    }
    
    fun inverse(): Direction {
        return when (this) {
            XN -> XP
            XP -> XN
            YN -> YP
            YP -> YN
            ZN -> ZP
            ZP -> ZN
        }
    }

    fun down(): Direction {
        return when (this) {
            YN, YP -> ZP
            else -> YN
        }
    }

    fun up(): Direction {
        return when (this) {
            YN, YP -> ZN
            else -> YP
        }
    }

    fun left(): Direction {
        return when (this) {
            YN -> XN
            YP -> XN
            ZN -> XN
            XN -> ZP
            ZP -> XP
            XP -> ZN
        }
    }

    fun right(): Direction {
        return when (this) {
            YN -> XP
            YP -> XP
            ZN -> XP
            XP -> ZP
            ZP -> XN
            XN -> ZN
        }
    }

    fun back(): Direction {
        return inverse()
    }

    fun applyLRDU(lrdu: LRDU): Direction {
        return when (lrdu) {
            LRDU.Left -> this.left()
            LRDU.Right -> this.right()
            LRDU.Up -> this.up()
            LRDU.Down -> this.down()
        }
    }
}

package mods.eln.misc

import net.minecraft.core.Direction as MCDirection
import org.lwjgl.opengl.GL11
import net.minecraft.world.phys.Vec3
import net.minecraft.nbt.CompoundTag
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis

typealias ForgeDirection = MCDirection

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

    fun right(): Direction {
        return when (this) {
            XN -> ZN
            XP -> ZP
            YN -> XP
            YP -> XN
            ZN -> XP
            ZP -> XN
        }
    }

    fun applyTo(pos: IntArray, factor: Int) {
        when (this) {
            XN -> pos[0] -= factor
            XP -> pos[0] += factor
            YN -> pos[1] -= factor
            YP -> pos[1] += factor
            ZN -> pos[2] -= factor
            ZP -> pos[2] += factor
        }
    }

    fun left(): Direction {
        return when (this) {
            XN -> ZP
            XP -> ZN
            YN -> XN
            YP -> XN
            ZN -> XN
            ZP -> XP
        }
    }

    val isY: Boolean get() = this == YN || this == YP
    val isNotY: Boolean get() = !isY

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

    fun rotateXnRef(poseStack: PoseStack) {
        when (this) {
            XN -> {}
            XP -> poseStack.mulPose(Axis.YP.rotationDegrees(180f))
            YN -> poseStack.mulPose(Axis.ZP.rotationDegrees(90f))
            YP -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90f))
            ZN -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f))
            ZP -> poseStack.mulPose(Axis.YP.rotationDegrees(90f))
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

    fun rotateZnRef(poseStack: PoseStack) {
        when (this) {
            ZN -> {}
            ZP -> poseStack.mulPose(Axis.YP.rotationDegrees(180f))
            YN -> poseStack.mulPose(Axis.XP.rotationDegrees(-90f))
            YP -> poseStack.mulPose(Axis.XP.rotationDegrees(90f))
            XN -> poseStack.mulPose(Axis.YP.rotationDegrees(90f))
            XP -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f))
        }
    }

    fun rotateZnRefInv(poseStack: PoseStack) {
        when (this) {
            ZN -> {}
            ZP -> poseStack.mulPose(Axis.YP.rotationDegrees(180f))
            YN -> poseStack.mulPose(Axis.XP.rotationDegrees(90f))
            YP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90f))
            XN -> poseStack.mulPose(Axis.YP.rotationDegrees(-90f))
            XP -> poseStack.mulPose(Axis.YP.rotationDegrees(90f))
        }
    }

    fun save(nbt: CompoundTag, name: String) {
        nbt.putByte(name, int.toByte())
    }

    companion object {
        @JvmField val N = ZN
        @JvmStatic
        fun fromIntMinecraftSide(side: Int): Direction {
            return when (side) {
                0 -> YN
                1 -> YP
                2 -> ZN
                3 -> ZP
                4 -> XN
                5 -> XP
                else -> YN
            }
        }

        fun fromInt(i: Int): Direction {
            return if (i in 0 until all.size) all[i] else XN
        }

        @JvmStatic
        fun fromMCDirection(dir: MCDirection): Direction {
            return fromIntMinecraftSide(dir.ordinal)
        }

        fun load(nbt: CompoundTag, name: String): Direction {
            return fromIntMinecraftSide(nbt.getByte(name).toInt())
        }

        val all = values()
        val axes = arrayOf(
            listOf(XN, XP),
            listOf(YN, YP),
            listOf(ZN, ZP)
        )
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

    fun up(): Direction {
        return when (this) {
            YN -> ZN
            YP -> ZN
            else -> YP
        }
    }

    fun down(): Direction {
        return up().inverse()
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

    fun getLRDUGoingTo(other: Direction): LRDU? {
        if (this.left() == other) return LRDU.Left
        if (this.right() == other) return LRDU.Right
        if (this.up() == other) return LRDU.Up
        if (this.down() == other) return LRDU.Down
        return null
    }

    fun rotatePose(poseStack: PoseStack) {
        when (this) {
            XN -> {}
            XP -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f))
            YN -> {
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90f))
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90f))
            }
            YP -> {
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90f))
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90f))
            }
            ZN -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90f))
            ZP -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90f))
        }
    }
}

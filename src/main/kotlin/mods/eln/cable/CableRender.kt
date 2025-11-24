package mods.eln.cable

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import mods.eln.misc.Direction
import mods.eln.misc.LRDU
import mods.eln.misc.LRDUMask
import mods.eln.node.NodeBase.Companion.isBlockWrappable
import mods.eln.node.NodeBlockEntity
import mods.eln.node.six.SixNodeElementRender
import mods.eln.node.six.SixNodeEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11

object CableRender {
    private fun putVertex(consumer: VertexConsumer, pose: Matrix4f, normal: Matrix3f, x: Float, y: Float, z: Float, u: Float, v: Float, nx: Float, ny: Float, nz: Float, light: Int, overlay: Int, r: Float, g: Float, b: Float, a: Float) {
        consumer.vertex(pose, x, y, z)
            .color(r, g, b, a)
            .uv(u, v)
            .overlayCoords(overlay)
            .uv2(light)
            .normal(normal, nx, ny, nz)
            .endVertex()
    }

    @JvmStatic
    fun connectionType(entity: NodeBlockEntity, connectedSide: LRDUMask, side: Direction): CableRenderType {
        var x2: Int
        var y2: Int
        var z2: Int
        val connectionTypeBuild = CableRenderType()
        var otherBlockEntity: BlockEntity?
        for (lrdu in LRDU.values()) {
            //noConnection
            if (!connectedSide[lrdu]) continue
            val sideLrdu = side.applyLRDU(lrdu)
            val pos = entity.blockPos
            x2 = pos.x
            y2 = pos.y
            z2 = pos.z
            when (sideLrdu) {
                Direction.XN -> x2--
                Direction.XP -> x2++
                Direction.YN -> y2--
                Direction.YP -> y2++
                Direction.ZN -> z2--
                Direction.ZP -> z2++
            }

            //standardConnection
            val pos2 = BlockPos(x2, y2, z2)
            otherBlockEntity = entity.level!!.getBlockEntity(pos2)
            if (otherBlockEntity is SixNodeEntity) {
                val sixNodeEntity = otherBlockEntity
                if (sixNodeEntity.elementRenderList[side.int] != null) {
                    val otherSide = side.applyLRDU(lrdu)
                    connectionTypeBuild.otherdry[lrdu.dir] =
                        sixNodeEntity.getCableDry(otherSide, otherSide.getLRDUGoingTo(side))
                    connectionTypeBuild.otherRender[lrdu.dir] =
                        sixNodeEntity.getCableRender(otherSide, otherSide.getLRDUGoingTo(side)!!)
                    continue
                }
            }

            //no wrappeConection ?
            val state = entity.level!!.getBlockState(pos2)
            if (!isBlockWrappable(state, entity.level!!, pos2)) {
                continue
            } else {
                when (side) {
                    Direction.XN -> x2--
                    Direction.XP -> x2++
                    Direction.YN -> y2--
                    Direction.YP -> y2++
                    Direction.ZN -> z2--
                    Direction.ZP -> z2++
                }
                val pos3 = BlockPos(x2, y2, z2)
                otherBlockEntity = entity.level!!.getBlockEntity(pos3)
                if (otherBlockEntity is NodeBlockEntity) {
                    val otherDirection = side.inverse()
                    val otherLRDU = otherDirection.getLRDUGoingTo(sideLrdu)!!.inverse()
                    val render = entity.getCableRender(sideLrdu, sideLrdu.getLRDUGoingTo(side)!!)
                    val otherNode = otherBlockEntity
                    val otherRender = otherNode.getCableRender(otherDirection, otherLRDU)
                    if (render == null) {
                        //Utils.println("ASSERT cableRender missing");
                        continue
                    }
                    if (otherRender == null) {
                        connectionTypeBuild.method[lrdu.dir] = CableRenderTypeMethodType.Etend
                        connectionTypeBuild.endAt[lrdu.dir] = render.heightPixel
                        connectionTypeBuild.otherdry[lrdu.dir] = otherNode.getCableDry(otherDirection, otherLRDU)
                        connectionTypeBuild.otherRender[lrdu.dir] = otherNode.getCableRender(otherDirection, otherLRDU)
                        continue
                    }
                    if (render.width == otherRender.width) {
                        if (sideLrdu.int > otherDirection.int) {
                            connectionTypeBuild.method[lrdu.dir] = CableRenderTypeMethodType.Etend
                            connectionTypeBuild.endAt[lrdu.dir] = otherRender.heightPixel
                        }
                        connectionTypeBuild.otherdry[lrdu.dir] = otherNode.getCableDry(otherDirection, otherLRDU)
                        connectionTypeBuild.otherRender[lrdu.dir] = otherNode.getCableRender(otherDirection, otherLRDU)
                        continue
                    }
                    if (render.width < otherRender.width) {
                        connectionTypeBuild.method[lrdu.dir] = CableRenderTypeMethodType.Etend
                        connectionTypeBuild.endAt[lrdu.dir] = otherRender.heightPixel
                        connectionTypeBuild.otherdry[lrdu.dir] = otherNode.getCableDry(otherDirection, otherLRDU)
                        connectionTypeBuild.otherRender[lrdu.dir] = otherNode.getCableRender(otherDirection, otherLRDU)
                        continue
                    }
                }
            }
        }
        return connectionTypeBuild
    }

    @JvmStatic
    fun connectionType(element: SixNodeElementRender, side: Direction): CableRenderType {
        var x2: Int
        var y2: Int
        var z2: Int
        val connectionTypeBuild = CableRenderType()
        var otherBlockEntity: BlockEntity?
        for (lrdu in LRDU.values()) {
            //noConnection
            if (!element.connectedSide[lrdu]) continue
            val sideLrdu = side.applyLRDU(lrdu)

            //InternalConnection
            if (element.blockEntity.elementRenderList[sideLrdu.int] != null) {
                val otherLRDU = sideLrdu.getLRDUGoingTo(side)
                val render = element.getCableRender(lrdu)
                val otherElement = element.blockEntity.elementRenderList[sideLrdu.int]!!
                val otherRender = otherElement.getCableRender(otherLRDU!!)
                if (otherRender == null || render == null) {
                    continue
                }
                if (render.width == otherRender.width) {
                    if (side.int > sideLrdu.int) {
                        connectionTypeBuild.method[lrdu.dir] = CableRenderTypeMethodType.Internal
                        connectionTypeBuild.endAt[lrdu.dir] = otherRender.heightPixel
                    }
                    connectionTypeBuild.otherdry[lrdu.dir] = otherElement.getCableDry(otherLRDU)
                    connectionTypeBuild.otherRender[lrdu.dir] = otherElement.getCableRender(otherLRDU)
                    continue
                }
                if (render.width < otherRender.width) {
                    connectionTypeBuild.method[lrdu.dir] = CableRenderTypeMethodType.Internal
                    connectionTypeBuild.endAt[lrdu.dir] = otherRender.heightPixel
                    connectionTypeBuild.otherdry[lrdu.dir] = otherElement.getCableDry(otherLRDU)
                    connectionTypeBuild.otherRender[lrdu.dir] = otherElement.getCableRender(otherLRDU)
                    continue
                }
                connectionTypeBuild.otherdry[lrdu.dir] = otherElement.getCableDry(otherLRDU)
                connectionTypeBuild.otherRender[lrdu.dir] = otherElement.getCableRender(otherLRDU)
                continue
            }
            val pos = element.blockEntity.blockPos
            x2 = pos.x
            y2 = pos.y
            z2 = pos.z
            when (sideLrdu) {
                Direction.XN -> x2--
                Direction.XP -> x2++
                Direction.YN -> y2--
                Direction.YP -> y2++
                Direction.ZN -> z2--
                Direction.ZP -> z2++
            }

            //standardConnection
            val pos2 = BlockPos(x2, y2, z2)
            otherBlockEntity = element.blockEntity.level!!.getBlockEntity(pos2)
            if (otherBlockEntity is SixNodeEntity) {
                val sixNodeEntity = otherBlockEntity
                if (sixNodeEntity.elementRenderList[side.int] != null) {
                    connectionTypeBuild.otherdry[lrdu.dir] =
                        sixNodeEntity.elementRenderList[side.int]!!.getCableDry(lrdu.inverse())
                    connectionTypeBuild.otherRender[lrdu.dir] =
                        sixNodeEntity.elementRenderList[side.int]!!.getCableRender(lrdu.inverse())
                    continue
                }
            }

            //no wrappeConection ?
            val state = element.blockEntity.level!!.getBlockState(pos2)
            if (!isBlockWrappable(
                    state,
                    element.blockEntity.level!!,
                    pos2
                )
            ) {
                continue
            } else {
                when (side) {
                    Direction.XN -> x2--
                    Direction.XP -> x2++
                    Direction.YN -> y2--
                    Direction.YP -> y2++
                    Direction.ZN -> z2--
                    Direction.ZP -> z2++
                }
                val pos3 = BlockPos(x2, y2, z2)
                otherBlockEntity = element.blockEntity.level!!.getBlockEntity(pos3)
                if (otherBlockEntity is NodeBlockEntity) {
                    val otherDirection = side.inverse()
                    val otherLRDU = otherDirection.getLRDUGoingTo(sideLrdu)!!.inverse()
                    val render = element.getCableRender(lrdu) ?: continue
                    val otherNode = otherBlockEntity
                    val otherRender = otherNode.getCableRender(otherDirection, otherLRDU)
                    if (otherRender == null) {
                        connectionTypeBuild.method[lrdu.dir] = CableRenderTypeMethodType.Etend
                        connectionTypeBuild.endAt[lrdu.dir] = render.heightPixel
                        connectionTypeBuild.otherdry[lrdu.dir] = otherNode.getCableDry(otherDirection, otherLRDU)
                        connectionTypeBuild.otherRender[lrdu.dir] = otherNode.getCableRender(otherDirection, otherLRDU)
                        continue
                    }
                    if (render.width == otherRender.width) {
                        if (sideLrdu.int > otherDirection.int) {
                            connectionTypeBuild.method[lrdu.dir] = CableRenderTypeMethodType.Etend
                            connectionTypeBuild.endAt[lrdu.dir] = otherRender.heightPixel
                        }
                        connectionTypeBuild.otherdry[lrdu.dir] = otherNode.getCableDry(otherDirection, otherLRDU)
                        connectionTypeBuild.otherRender[lrdu.dir] = otherNode.getCableRender(otherDirection, otherLRDU)
                        continue
                    }
                    if (render.width < otherRender.width) {
                        connectionTypeBuild.method[lrdu.dir] = CableRenderTypeMethodType.Etend
                        connectionTypeBuild.endAt[lrdu.dir] = otherRender.heightPixel
                        connectionTypeBuild.otherdry[lrdu.dir] = otherNode.getCableDry(otherDirection, otherLRDU)
                        connectionTypeBuild.otherRender[lrdu.dir] = otherNode.getCableRender(otherDirection, otherLRDU)
                        continue
                    }
                    connectionTypeBuild.otherdry[lrdu.dir] = otherNode.getCableDry(otherDirection, otherLRDU)
                    connectionTypeBuild.otherRender[lrdu.dir] = otherNode.getCableRender(otherDirection, otherLRDU)
                    continue
                }
            }
        }
        return connectionTypeBuild
    }



    private fun drawQuad(
        consumer: VertexConsumer,
        pose: Matrix4f,
        normal: Matrix3f,
        light: Int,
        overlay: Int,
        v0: FloatArray,
        v1: FloatArray,
        v2: FloatArray,
        v3: FloatArray,
        nx: Float,
        ny: Float,
        nz: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        putVertex(consumer, pose, normal, v0[0], v0[1], v0[2], v0[3], v0[4], nx, ny, nz, light, overlay, r, g, b, a)
        putVertex(consumer, pose, normal, v1[0], v1[1], v1[2], v1[3], v1[4], nx, ny, nz, light, overlay, r, g, b, a)
        putVertex(consumer, pose, normal, v3[0], v3[1], v3[2], v3[3], v3[4], nx, ny, nz, light, overlay, r, g, b, a)
        putVertex(consumer, pose, normal, v2[0], v2[1], v2[2], v2[3], v2[4], nx, ny, nz, light, overlay, r, g, b, a)
    }

    private fun drawQuadSimple(
        consumer: VertexConsumer,
        pose: Matrix4f,
        normal: Matrix3f,
        light: Int,
        overlay: Int,
        v0: FloatArray,
        v1: FloatArray,
        v2: FloatArray,
        v3: FloatArray,
        nx: Float,
        ny: Float,
        nz: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        putVertex(consumer, pose, normal, v0[0], v0[1], v0[2], v0[3], v0[4], nx, ny, nz, light, overlay, r, g, b, a)
        putVertex(consumer, pose, normal, v1[0], v1[1], v1[2], v1[3], v1[4], nx, ny, nz, light, overlay, r, g, b, a)
        putVertex(consumer, pose, normal, v2[0], v2[1], v2[2], v2[3], v2[4], nx, ny, nz, light, overlay, r, g, b, a)
        putVertex(consumer, pose, normal, v3[0], v3[1], v3[2], v3[3], v3[4], nx, ny, nz, light, overlay, r, g, b, a)
    }

    @JvmStatic
    fun drawCable(
        poseStack: PoseStack,
        consumer: VertexConsumer,
        light: Int,
        overlay: Int,
        cable: CableRenderDescriptor?,
        connection: LRDUMask,
        connectionType: CableRenderType,
        deltaStart: Float = cable!!.widthDiv2 / 2f,
        drawBottom: Boolean = false,
        r: Float = 1f, g: Float = 1f, b: Float = 1f, a: Float = 1f
    ) {
        if (cable == null) return
        val pose = poseStack.last().pose()
        val normal = poseStack.last().normal()

        var endLeft = -deltaStart
        var endRight = deltaStart
        var endUp = deltaStart
        var endDown = -deltaStart
        val startLeft = -connectionType.startAt[0]
        val startRight = connectionType.startAt[1]
        val startUp = connectionType.startAt[2]
        val startDown = -connectionType.startAt[3]
        if ((connection.mask == 0) and (deltaStart >= 0f)) {
            endLeft = -cable.widthDiv2 - 3.0f / 16.0f
            endRight = cable.widthDiv2 + 3.0f / 16.0f
            endDown = -cable.widthDiv2 - 3.0f / 16.0f
            endUp = cable.widthDiv2 + 3.0f / 16.0f
        } else {
            if (connection[LRDU.Left]) {
                endLeft = -0.5f
            }
            if (connection[LRDU.Right]) {
                endRight = 0.5f
            }
            if (connection[LRDU.Down]) {
                endDown = -0.5f
            }
            if (connection[LRDU.Up]) {
                endUp = 0.5f
            }
        }
        when (connectionType.method[0]) {
            CableRenderTypeMethodType.Internal -> endLeft += (connectionType.endAt[0] / 16.0).toFloat()
            CableRenderTypeMethodType.Etend -> endLeft -= (connectionType.endAt[0] / 16.0).toFloat()
            else -> {}
        }
        when (connectionType.method[1]) {
            CableRenderTypeMethodType.Internal -> endRight -= (connectionType.endAt[1] / 16.0).toFloat()
            CableRenderTypeMethodType.Etend -> endRight += (connectionType.endAt[1] / 16.0).toFloat()
            else -> {}
        }
        when (connectionType.method[2]) {
            CableRenderTypeMethodType.Internal -> endDown += (connectionType.endAt[2] / 16.0).toFloat()
            CableRenderTypeMethodType.Etend -> endDown -= (connectionType.endAt[2] / 16.0).toFloat()
            else -> {}
        }
        when (connectionType.method[3]) {
            CableRenderTypeMethodType.Internal -> endUp -= (connectionType.endAt[3] / 16.0).toFloat()
            CableRenderTypeMethodType.Etend -> endUp += (connectionType.endAt[3] / 16.0).toFloat()
            else -> {}
        }
        val height = cable.height
        val tx = 0.25f
        val ty = 0.5f

        if (endLeft < startLeft) {
            // Draws top, bottom, and two sides of the cable
            // GL_QUAD_STRIP replacement
            // v0, v1, v2, v3, v4, v5, v6, v7...
            // Q1: v0, v1, v3, v2
            // Q2: v2, v3, v5, v4
            // Q3: v4, v5, v7, v6
            
            val v0 = floatArrayOf(0f, cable.widthDiv2, endLeft, tx + (cable.widthDiv2 + height) * 0.5f, ty + endLeft)
            val v1 = floatArrayOf(0f, cable.widthDiv2, startLeft, tx + (cable.widthDiv2 + height) * 0.5f, ty + startLeft)
            val v2 = floatArrayOf(height, cable.widthDiv2, endLeft, tx + cable.widthDiv2 * 0.5f, ty + endLeft)
            val v3 = floatArrayOf(height, cable.widthDiv2, startLeft, tx + cable.widthDiv2 * 0.5f, ty + startLeft)
            val v4 = floatArrayOf(height, -cable.widthDiv2, endLeft, tx - cable.widthDiv2 * 0.5f, ty + endLeft)
            val v5 = floatArrayOf(height, -cable.widthDiv2, startLeft, tx - cable.widthDiv2 * 0.5f, ty + startLeft)
            val v6 = floatArrayOf(0f, -cable.widthDiv2, endLeft, tx - cable.widthDiv2 * 0.5f - height, ty + endLeft)
            val v7 = floatArrayOf(0f, -cable.widthDiv2, startLeft, tx - cable.widthDiv2 * 0.5f - height, ty + startLeft)
            
            // Normal 0, 1, 0
            drawQuad(consumer, pose, normal, light, overlay, v0, v1, v2, v3, 0f, 1f, 0f, r, g, b, a)
            // Normal 1, 0, 0
            drawQuad(consumer, pose, normal, light, overlay, v2, v3, v4, v5, 1f, 0f, 0f, r, g, b, a)
            // Normal 0, -1, 0
            drawQuad(consumer, pose, normal, light, overlay, v4, v5, v6, v7, 0f, -1f, 0f, r, g, b, a)

            if (drawBottom) {
                val v8 = floatArrayOf(0f, cable.widthDiv2, endLeft, tx + (cable.widthDiv2 + height) * 0.5f, ty + endLeft)
                val v9 = floatArrayOf(0f, cable.widthDiv2, startLeft, tx + (cable.widthDiv2 + height) * 0.5f, ty + startLeft)
                drawQuad(consumer, pose, normal, light, overlay, v6, v7, v9, v8, 0f, 1f, 0f, r, g, b, a)
            }

            // Draws end cap
            // GL_QUADS
            val c0 = floatArrayOf(0f, -cable.widthDiv2, endLeft, tx - cable.widthDiv2 * 0.5f, ty + endLeft - height)
            val c1 = floatArrayOf(0f, cable.widthDiv2, endLeft, tx + cable.widthDiv2 * 0.5f, ty + endLeft - height)
            val c2 = floatArrayOf(height, cable.widthDiv2, endLeft, tx + cable.widthDiv2 * 0.5f, ty + endLeft)
            val c3 = floatArrayOf(height, -cable.widthDiv2, endLeft, tx - cable.widthDiv2 * 0.5f, ty + endLeft)
            
            drawQuadSimple(consumer, pose, normal, light, overlay, c0, c1, c2, c3, 0f, 0f, -1f, r, g, b, a)
        }
        
        // Draw Right
        if (endRight > startRight) {
             val v0 = floatArrayOf(0f, cable.widthDiv2, startRight, tx + (cable.widthDiv2 + height) * 0.5f, ty + startRight)
             val v1 = floatArrayOf(0f, cable.widthDiv2, endRight, tx + (cable.widthDiv2 + height) * 0.5f, ty + endRight)
             val v2 = floatArrayOf(height, cable.widthDiv2, startRight, tx + cable.widthDiv2 * 0.5f, ty + startRight)
             val v3 = floatArrayOf(height, cable.widthDiv2, endRight, tx + cable.widthDiv2 * 0.5f, ty + endRight)
             val v4 = floatArrayOf(height, -cable.widthDiv2, startRight, tx - cable.widthDiv2 * 0.5f, ty + startRight)
             val v5 = floatArrayOf(height, -cable.widthDiv2, endRight, tx - cable.widthDiv2 * 0.5f, ty + endRight)
             val v6 = floatArrayOf(0f, -cable.widthDiv2, startRight, tx - cable.widthDiv2 * 0.5f - height, ty + startRight)
             val v7 = floatArrayOf(0f, -cable.widthDiv2, endRight, tx - cable.widthDiv2 * 0.5f - height, ty + endRight)

             drawQuad(consumer, pose, normal, light, overlay, v0, v1, v2, v3, 0f, 1f, 0f, r, g, b, a)
             drawQuad(consumer, pose, normal, light, overlay, v2, v3, v4, v5, 1f, 0f, 0f, r, g, b, a)
             drawQuad(consumer, pose, normal, light, overlay, v4, v5, v6, v7, 0f, -1f, 0f, r, g, b, a)
             
             if (drawBottom) {
                 val v8 = floatArrayOf(0f, cable.widthDiv2, startRight, tx + (cable.widthDiv2 + height) * 0.5f, ty + startRight)
                 val v9 = floatArrayOf(0f, cable.widthDiv2, endRight, tx + (cable.widthDiv2 + height) * 0.5f, ty + endRight)
                 drawQuad(consumer, pose, normal, light, overlay, v6, v7, v9, v8, 0f, 1f, 0f, r, g, b, a)
             }
             
             // End cap
             val c0 = floatArrayOf(height, -cable.widthDiv2, endRight, tx - cable.widthDiv2 * 0.5f, ty + endRight)
             val c1 = floatArrayOf(height, cable.widthDiv2, endRight, tx + cable.widthDiv2 * 0.5f, ty + endRight)
             val c2 = floatArrayOf(0f, cable.widthDiv2, endRight, tx + cable.widthDiv2 * 0.5f, ty + endRight + height)
             val c3 = floatArrayOf(0f, -cable.widthDiv2, endRight, tx - cable.widthDiv2 * 0.5f, ty + endRight + height)
             drawQuadSimple(consumer, pose, normal, light, overlay, c0, c1, c2, c3, 0f, 0f, 1f, r, g, b, a)
        }

        if (endDown < startDown) {
             val v0 = floatArrayOf(0f, endDown, -cable.widthDiv2, tx - (cable.widthDiv2 - height) * 0.5f, ty + endDown)
             val v1 = floatArrayOf(0f, startDown, -cable.widthDiv2, tx - (cable.widthDiv2 - height) * 0.5f, ty + startDown)
             val v2 = floatArrayOf(height, endDown, -cable.widthDiv2, tx - cable.widthDiv2 * 0.5f, ty + endDown)
             val v3 = floatArrayOf(height, startDown, -cable.widthDiv2, tx - cable.widthDiv2 * 0.5f, ty + startDown)
             val v4 = floatArrayOf(height, endDown, cable.widthDiv2, tx + cable.widthDiv2 * 0.5f, ty + endDown)
             val v5 = floatArrayOf(height, startDown, cable.widthDiv2, tx + cable.widthDiv2 * 0.5f, ty + startDown)
             val v6 = floatArrayOf(0f, endDown, cable.widthDiv2, tx + (cable.widthDiv2 + height) * 0.5f, ty + endDown)
             val v7 = floatArrayOf(0f, startDown, cable.widthDiv2, tx + (cable.widthDiv2 + height) * 0.5f, ty + startDown)

             drawQuad(consumer, pose, normal, light, overlay, v0, v1, v2, v3, 0f, 1f, 0f, r, g, b, a)
             drawQuad(consumer, pose, normal, light, overlay, v2, v3, v4, v5, 1f, 0f, 0f, r, g, b, a)
             drawQuad(consumer, pose, normal, light, overlay, v4, v5, v6, v7, 0f, -1f, 0f, r, g, b, a)
             
             if (drawBottom) {
                 val v8 = floatArrayOf(0f, endDown, -cable.widthDiv2, tx - (cable.widthDiv2 - height) * 0.5f, ty + endDown)
                 val v9 = floatArrayOf(0f, startDown, -cable.widthDiv2, tx - (cable.widthDiv2 - height) * 0.5f, ty + startDown)
                 drawQuad(consumer, pose, normal, light, overlay, v6, v7, v9, v8, 0f, 0f, -1f, r, g, b, a)
             }
             
             // End cap
             val c0 = floatArrayOf(height, endDown, -cable.widthDiv2, tx - cable.widthDiv2 * 0.5f, ty + endDown)
             val c1 = floatArrayOf(height, endDown, cable.widthDiv2, tx + cable.widthDiv2 * 0.5f, ty + endDown)
             val c2 = floatArrayOf(0f, endDown, cable.widthDiv2, tx + cable.widthDiv2 * 0.5f, ty + endDown - height)
             val c3 = floatArrayOf(0f, endDown, -cable.widthDiv2, tx - cable.widthDiv2 * 0.5f, ty + endDown - height)
             drawQuadSimple(consumer, pose, normal, light, overlay, c0, c1, c2, c3, 0f, -1f, 0f, r, g, b, a)
        }

        if (endUp > startUp) {
             val v0 = floatArrayOf(0f, startUp, -cable.widthDiv2, tx - (cable.widthDiv2 - height) * 0.5f, ty + startUp)
             val v1 = floatArrayOf(0f, endUp, -cable.widthDiv2, tx - (cable.widthDiv2 - height) * 0.5f, ty + endUp)
             val v2 = floatArrayOf(height, startUp, -cable.widthDiv2, tx - cable.widthDiv2 * 0.5f, ty + startUp)
             val v3 = floatArrayOf(height, endUp, -cable.widthDiv2, tx - cable.widthDiv2 * 0.5f, ty + endUp)
             val v4 = floatArrayOf(height, startUp, cable.widthDiv2, tx + cable.widthDiv2 * 0.5f, ty + startUp)
             val v5 = floatArrayOf(height, endUp, cable.widthDiv2, tx + cable.widthDiv2 * 0.5f, ty + endUp)
             val v6 = floatArrayOf(0f, startUp, cable.widthDiv2, tx + (cable.widthDiv2 + height) * 0.5f, ty + startUp)
             val v7 = floatArrayOf(0f, endUp, cable.widthDiv2, tx + (cable.widthDiv2 + height) * 0.5f, ty + endUp)

             drawQuad(consumer, pose, normal, light, overlay, v0, v1, v2, v3, 0f, 0f, -1f, r, g, b, a)
             drawQuad(consumer, pose, normal, light, overlay, v2, v3, v4, v5, 1f, 0f, 0f, r, g, b, a)
             drawQuad(consumer, pose, normal, light, overlay, v4, v5, v6, v7, 0f, 0f, 1f, r, g, b, a)
             
             if (drawBottom) {
                 val v8 = floatArrayOf(0f, startUp, -cable.widthDiv2, tx - (cable.widthDiv2 - height) * 0.5f, ty + startUp)
                 val v9 = floatArrayOf(0f, endUp, -cable.widthDiv2, tx - (cable.widthDiv2 - height) * 0.5f, ty + endUp)
                 drawQuad(consumer, pose, normal, light, overlay, v6, v7, v9, v8, 0f, 0f, -1f, r, g, b, a)
             }
             
             // End cap
             val c0 = floatArrayOf(0f, endUp, cable.widthDiv2, tx - cable.widthDiv2 * 0.5f, ty + endUp + height)
             val c1 = floatArrayOf(0f, endUp, -cable.widthDiv2, tx + cable.widthDiv2 * 0.5f, ty + endUp + height)
             val c2 = floatArrayOf(height, endUp, -cable.widthDiv2, tx + cable.widthDiv2 * 0.5f, ty + endUp)
             val c3 = floatArrayOf(height, endUp, cable.widthDiv2, tx - cable.widthDiv2 * 0.5f, ty + endUp)
             drawQuadSimple(consumer, pose, normal, light, overlay, c0, c1, c2, c3, 0f, 1f, 0f, r, g, b, a)
        }
    }



    @JvmStatic
    fun drawNode(
        poseStack: PoseStack,
        consumer: VertexConsumer,
        light: Int,
        overlay: Int,
        cable: CableRenderDescriptor,
        connection: LRDUMask,
        connectionType: CableRenderType?,
        r: Float = 1f, g: Float = 1f, b: Float = 1f, a: Float = 1f
    ) {
        if (connection.mask == 0 || (connection[LRDU.Left] || connection[LRDU.Right]) && (connection[LRDU.Down] || connection[LRDU.Up]) || connection.mask == 1 || connection.mask == 2 || connection.mask == 4 || connection.mask == 8) {
            val widthDiv2 = cable.widthDiv2 + 1.0f / 16.0f
            val height = cable.height + 1.0f / 16.0f
            val tx = 0.75f
            val ty = 0.5f
            
            val pose = poseStack.last().pose()
            val normal = poseStack.last().normal()

            // Quad 1 (Top)
            val q1v0 = floatArrayOf(0f, widthDiv2, -widthDiv2, tx + (widthDiv2 + cable.height + 1.0f / 16.0f) * 0.5f, ty - widthDiv2)
            val q1v1 = floatArrayOf(0f, widthDiv2, widthDiv2, tx + (widthDiv2 + cable.height + 1.0f / 16.0f) * 0.5f, ty + widthDiv2)
            val q1v2 = floatArrayOf(height, widthDiv2, -widthDiv2, tx + widthDiv2 * 0.5f, ty - widthDiv2)
            val q1v3 = floatArrayOf(height, widthDiv2, widthDiv2, tx + widthDiv2 * 0.5f, ty + widthDiv2)
            drawQuad(consumer, pose, normal, light, overlay, q1v0, q1v1, q1v3, q1v2, 0f, 1f, 0f, r, g, b, a)

            // Quad 2 (Right)
            val q2v0 = q1v2
            val q2v1 = q1v3
            val q2v2 = floatArrayOf(height, -widthDiv2, -widthDiv2, tx - widthDiv2 * 0.5f, ty - widthDiv2)
            val q2v3 = floatArrayOf(height, -widthDiv2, widthDiv2, tx - widthDiv2 * 0.5f, ty + widthDiv2)
            drawQuad(consumer, pose, normal, light, overlay, q2v0, q2v1, q2v3, q2v2, 1f, 0f, 0f, r, g, b, a)

            // Quad 3 (Bottom)
            val q3v0 = q2v2
            val q3v1 = q2v3
            val q3v2 = floatArrayOf(0f, -widthDiv2, -widthDiv2, tx - (widthDiv2 + cable.height + 1.0f / 16.0f) * 0.5f, ty - widthDiv2)
            val q3v3 = floatArrayOf(0f, -widthDiv2, widthDiv2, tx - (widthDiv2 + cable.height + 1.0f / 16.0f) * 0.5f, ty + widthDiv2)
            drawQuad(consumer, pose, normal, light, overlay, q3v0, q3v1, q3v3, q3v2, 0f, -1f, 0f, r, g, b, a)

            // Back
            val b0 = floatArrayOf(0f, -widthDiv2, -widthDiv2, tx - widthDiv2 * 0.5f, ty - widthDiv2 - cable.height - 1.0f / 16.0f)
            val b1 = floatArrayOf(0f, widthDiv2, -widthDiv2, tx + widthDiv2 * 0.5f, ty - widthDiv2 - cable.height - 1.0f / 16.0f)
            val b2 = floatArrayOf(height, widthDiv2, -widthDiv2, tx + widthDiv2 * 0.5f, ty - widthDiv2)
            val b3 = floatArrayOf(height, -widthDiv2, -widthDiv2, tx - widthDiv2 * 0.5f, ty - widthDiv2)
            drawQuad(consumer, pose, normal, light, overlay, b0, b1, b2, b3, 0f, 0f, -1f, r, g, b, a)

            // Front
            val f0 = floatArrayOf(height, -widthDiv2, widthDiv2, tx - widthDiv2 * 0.5f, ty + widthDiv2)
            val f1 = floatArrayOf(height, widthDiv2, widthDiv2, tx + widthDiv2 * 0.5f, ty + widthDiv2)
            val f2 = floatArrayOf(0f, widthDiv2, widthDiv2, tx + widthDiv2 * 0.5f, ty + widthDiv2 + cable.height + 1.0f / 16.0f)
            val f3 = floatArrayOf(0f, -widthDiv2, widthDiv2, tx - widthDiv2 * 0.5f, ty + widthDiv2 + cable.height + 1.0f / 16.0f)
            drawQuad(consumer, pose, normal, light, overlay, f0, f1, f2, f3, 0f, 0f, 1f, r, g, b, a)
        }
    }
}
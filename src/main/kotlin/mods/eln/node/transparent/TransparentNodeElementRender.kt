@file:Suppress("NAME_SHADOWING")
package mods.eln.node.transparent

import mods.eln.cable.CableRender
import mods.eln.cable.CableRenderDescriptor
import mods.eln.cable.CableRenderType
import mods.eln.client.ClientProxy
import mods.eln.misc.Coordinate
import mods.eln.misc.Direction
import mods.eln.misc.Direction.Companion.fromInt
import mods.eln.misc.LRDU
import mods.eln.misc.LRDUMask
import mods.eln.misc.Utils.unserializeItemStackToItemEntity
import mods.eln.misc.UtilsClient.setGlColorFromDye
import mods.eln.misc.UtilsClient
import mods.eln.sound.LoopedSound
import mods.eln.sound.LoopedSoundManager
import mods.eln.sound.SoundCommand
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import org.lwjgl.opengl.GL11
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

abstract class TransparentNodeElementRender(var tileEntity: TransparentNodeBlockEntity, var transparentNodedescriptor: TransparentNodeDescriptor) {
    @JvmField
    var front: Direction? = null
    var grounded = false
    var currentPoseStack: com.mojang.blaze3d.vertex.PoseStack? = null
    var currentBuffer: net.minecraft.client.renderer.MultiBufferSource? = null
    var currentLight: Int = 0
    var currentOverlay: Int = 0
    @Throws(IOException::class)
    protected fun unserializeItemStackToItemEntity(stream: DataInputStream?, old: ItemEntity?): ItemEntity? {
        return unserializeItemStackToItemEntity(stream!!, old, tileEntity)
    }

    fun drawItemEntity(entityItem: ItemEntity?, x: Double, y: Double, z: Double, roty: Float, scale: Float) {
        UtilsClient.drawItemEntity(entityItem, x, y, z, roty, scale)
    }

    fun glCableTransform(inverse: Direction) {
        val vec = inverse.toMCDirection().normal
        GL11.glTranslatef(vec.x * 0.5f, vec.y * 0.5f, vec.z * 0.5f)
        inverse.glRotateXnRef()
    }

    fun glCableTransform(poseStack: com.mojang.blaze3d.vertex.PoseStack, inverse: Direction) {
        val vec = inverse.toMCDirection().normal
        poseStack.translate(vec.x * 0.5f, vec.y * 0.5f, vec.z * 0.5f)
        inverse.rotateXnRef(poseStack)
    }

    abstract fun draw()

    open fun render(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        draw()
    }

    open fun networkUnserialize(stream: DataInputStream) {
        try {
            val b = stream.readByte()
            front = fromInt(b.toInt() and 0x7)
            grounded = b.toInt() and 8 != 0
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    open fun newGuiDraw(side: Direction, player: Player): Screen? {
        return null
    }

    open val inventory: Container?
        get() = null

    fun preparePacketForServer(stream: DataOutputStream?) {
        tileEntity.preparePacketForServer(stream!!)
    }

    fun sendPacketToServer(bos: ByteArrayOutputStream?) {
        tileEntity.sendPacketToServer(bos)
    }

    fun clientSetGrounded(value: Boolean) {
        clientSendBoolean(TransparentNodeElement.unserializeGroundedId, value)
    }

    fun clientSendBoolean(id: Byte, value: Boolean) {
        try {
            val bos = ByteArrayOutputStream()
            val stream = DataOutputStream(bos)
            preparePacketForServer(stream)
            // Note: preparePacketForServer already writes elementRenderId
            stream.writeByte(id.toInt())
            stream.writeByte(if (value) 1 else 0)
            sendPacketToServer(bos)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun clientSendId(id: Byte) {
        try {
            val bos = ByteArrayOutputStream()
            val stream = DataOutputStream(bos)
            preparePacketForServer(stream)
            // Note: preparePacketForServer already writes elementRenderId
            stream.writeByte(id.toInt())
            sendPacketToServer(bos)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun clientSendString(id: Byte, str: String) {
        try {
            val bos = ByteArrayOutputStream()
            val stream = DataOutputStream(bos)
            preparePacketForServer(stream)
            // Note: preparePacketForServer already writes elementRenderId
            stream.writeByte(id.toInt())
            stream.writeUTF(str)
            sendPacketToServer(bos)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun clientSendFloat(id: Byte, value: Float) {
        try {
            val bos = ByteArrayOutputStream()
            val stream = DataOutputStream(bos)
            preparePacketForServer(stream)
            // Note: preparePacketForServer already writes elementRenderId
            stream.writeByte(id.toInt())
            stream.writeFloat(value)
            sendPacketToServer(bos)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun clientSendInt(id: Byte, str: Int) {
        try {
            val bos = ByteArrayOutputStream()
            val stream = DataOutputStream(bos)
            preparePacketForServer(stream)
            // Note: preparePacketForServer already writes elementRenderId
            stream.writeByte(id.toInt())
            stream.writeInt(str)
            sendPacketToServer(bos)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    open fun cameraDrawOptimisation(): Boolean {
        return true
    }

    open fun getCableRenderSide(side: Direction, lrdu: LRDU): CableRenderDescriptor? {
        return null
    }

    fun drawCable(poseStack: com.mojang.blaze3d.vertex.PoseStack, bufferSource: net.minecraft.client.renderer.MultiBufferSource, packedLight: Int, packedOverlay: Int, side: Direction, render: CableRenderDescriptor?, connection: LRDUMask, renderPreProcess: CableRenderType?, drawBottom: Boolean = false): CableRenderType? {
        var renderPreProcess = renderPreProcess
        if (render == null) return renderPreProcess
        if (renderPreProcess == null) renderPreProcess = CableRender.connectionType(tileEntity, connection, side)

        poseStack.pushPose()
        glCableTransform(poseStack, side)

        val consumer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityCutout(render.cableTexture))

        for (lrdu in LRDU.values()) {
            val color = net.minecraft.world.item.DyeColor.byId(renderPreProcess.otherdry[lrdu.toInt()]).textureDiffuseColors
            if (!connection[lrdu]) continue
            maskTempDraw.set(1 shl lrdu.toInt())
            CableRender.drawCable(poseStack, consumer, packedLight, packedOverlay, render, maskTempDraw, renderPreProcess, render.widthDiv2 / 2f, drawBottom, color[0], color[1], color[2], 1f)
        }
        poseStack.popPose()
        return renderPreProcess
    }

    open fun notifyNeighborSpawn() {}
    open fun serverPacketUnserialize(stream: DataInputStream?) {}
    protected fun coordinate(): Coordinate {
        return Coordinate(tileEntity)
    }

    private var uuid = 0
    fun getUuid(): Int {
        if (uuid == 0) {
            uuid = UtilsClient.uuid
        }
        return uuid
    }

    fun usedUuid(): Boolean {
        return uuid != 0
    }

    fun play(s: SoundCommand) {
        s.addUuid(getUuid())
        s.set(tileEntity)
        s.play()
    }

    private val loopedSoundManager = LoopedSoundManager()
    @OnlyIn(Dist.CLIENT)
    protected fun addLoopedSound(loopedSound: LoopedSound?) {
        loopedSoundManager.add(loopedSound)
    }

    fun destructor() {
        if (usedUuid()) ClientProxy.uuidManager.kill(uuid)
        loopedSoundManager.dispose()
    }

    open fun refresh(deltaT: Float) {
        loopedSoundManager.process(deltaT)
    }

    companion object {
        val maskTempDraw = LRDUMask()
    }
}

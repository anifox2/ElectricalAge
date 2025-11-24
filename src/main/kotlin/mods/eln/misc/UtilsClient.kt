@file:Suppress("NAME_SHADOWING", "DEPRECATION")
package mods.eln.misc

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import mods.eln.Eln
import mods.eln.ElnNetwork
import mods.eln.ElnPacket

import mods.eln.misc.Obj3D.Obj3DPart
import mods.eln.node.six.SixNodeEntity
import mods.eln.sim.ThermalLoad
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import org.lwjgl.opengl.GL11
import kotlin.math.sqrt
import mods.eln.node.transparent.TransparentNodeBlockEntity

object UtilsClient {
    @JvmField
    var guiLastOpen: Screen? = null
    var lightmapTexUnitTextureEnable = false

    // Rendering context for 1.20 port
    var currentPoseStack: PoseStack? = null
    var currentBufferSource: MultiBufferSource? = null
    var currentPackedLight: Int = 0
    var currentPackedOverlay: Int = 0
    
    @JvmStatic
    var uuid = Int.MIN_VALUE
        get() {
            if (field > -1) field = Int.MIN_VALUE
            return field++
        }
        private set
    val whiteTexture = ResourceLocation("eln", "sprites/cable.png")
    val portableBatteryOverlayResource = ResourceLocation("eln", "sprites/portablebatteryoverlay.png")
    
    fun drawItemEntity(entityItem: ItemEntity?, x: Double, y: Double, z: Double, roty: Float, scale: Float) {
        if (entityItem == null) return
        val poseStack = currentPoseStack ?: return
        val bufferSource = currentBufferSource ?: return
        
        poseStack.pushPose()
        poseStack.translate(x, y, z)
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(roty))
        poseStack.scale(scale, scale, scale)
        
        Minecraft.getInstance().entityRenderDispatcher.render(
            entityItem, 0.0, 0.0, 0.0, 0.0f, 0.0f,
            poseStack, bufferSource, currentPackedLight
        )
        
        poseStack.popPose()
    }

    @JvmStatic
    fun distanceFromClientPlayer(world: Level?, x: Int, y: Int, z: Int): Float {
        val player = Minecraft.getInstance().player ?: return 0f
        return sqrt((x - player.x) * (x - player.x) + (y - player.y) * (y - player.y) + (z - player.z) * (z - player.z)).toFloat()
    }

    @JvmStatic
    fun distanceFromClientPlayer(entity: Entity?): Float {
        if (entity == null) return 0f
        return distanceFromClientPlayer(entity.level(), entity.blockPosition().x, entity.blockPosition().y, entity.blockPosition().z)
    }

    @JvmStatic
    fun drawHaloNoLightSetup(halo: ResourceLocation, r: Float, g: Float, b: Float, entity: Entity, bilinear: Boolean) {
        // Texture based halo not implemented yet
    }

    @JvmStatic
    fun distanceFromClientPlayer(tileEntity: SixNodeEntity): Float {
        val pos = tileEntity.blockPos
        return distanceFromClientPlayer(tileEntity.level, pos.x, pos.y, pos.z)
    }

    val clientPlayer: net.minecraft.client.player.LocalPlayer?
        get() = Minecraft.getInstance().player

    @JvmStatic
    fun drawHalo(halo: Obj3DPart?, r: Float, g: Float, b: Float, e: BlockEntity, bilinear: Boolean) {
        val pos = e.blockPos
        drawHalo(halo, r, g, b, e.level, pos.x, pos.y, pos.z, bilinear)
    }

    @JvmStatic
    fun drawHalo(halo: Obj3DPart?, r: Float, g: Float, b: Float, level: Level?, x: Int, y: Int, z: Int, bilinear: Boolean) {
        if (halo == null) return
        // Assume matrix is already set up if called from render
        
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glColor4f(r, g, b, 1.0f)
        
        halo.draw()
        
        GL11.glEnable(GL11.GL_LIGHTING)
        RenderSystem.disableBlend()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    }

    @JvmStatic
    fun drawHaloNoLightSetup(halo: Obj3DPart?, @Suppress("UNUSED_PARAMETER") distance: Float) {
        if (halo == null) return
        // halo.faceGroup[0].bindTexture() // TODO: Fix texture binding
        enableBilinear()
        // halo.drawNoBind() // TODO: Fix drawing
    }

    @JvmStatic
    fun drawHalo(halo: Obj3DPart?, distance: Float) {
        disableLight()
        enableBlend()
        drawHaloNoLightSetup(halo, distance)
        enableLight()
        disableBlend()
    }

    @JvmStatic
    fun drawHaloNoLightSetup(halo: Obj3DPart?, r: Float, g: Float, b: Float, entity: BlockEntity?, bilinear: Boolean) {
        // Placeholder
    }

    @JvmStatic
    fun drawHaloNoLightSetup(halo: Obj3DPart?, r: Float, g: Float, b: Float, entity: Entity?, bilinear: Boolean) {
        // Placeholder
    }

    @JvmStatic
    fun drawHalo(halo: Obj3DPart?, r: Float, g: Float, b: Float, e: Entity, bilinear: Boolean) {
        disableLight()
        enableBlend()
        drawHaloNoLightSetup(halo, r, g, b, e, bilinear)
        enableLight()
        disableBlend()
    }

    @JvmStatic
    fun enableBilinear() {
        // RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        // RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
    }

    @JvmStatic
    fun disableBilinear() {
        // RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        // RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
    }

    @JvmStatic
    fun disableCulling() {
        RenderSystem.disableCull()
    }

    @JvmStatic
    fun enableCulling() {
        RenderSystem.enableCull()
    }

    @JvmStatic
    fun disableTexture() {
        // RenderSystem.disableTexture()
    }

    @JvmStatic
    fun enableTexture() {
        // RenderSystem.enableTexture()
    }

    @JvmStatic
    fun disableLight() {
        // RenderSystem.disableLighting()
    }

    @JvmStatic
    fun enableLight() {
        // RenderSystem.enableLighting()
    }

    @JvmStatic
    fun enableBlend() {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
    }

    @JvmStatic
    fun disableBlend() {
        RenderSystem.disableBlend()
    }

    @JvmStatic
    fun bindTexture(resource: ResourceLocation?) {
        if (resource != null) {
            RenderSystem.setShaderTexture(0, resource)
        }
    }

    @JvmStatic
    fun ledOnOffColor(on: Boolean) {
        if (!on) RenderSystem.setShaderColor(0.7f, 0f, 0f, 1f) // Red
        else RenderSystem.setShaderColor(0f, 0.7f, 0f, 1f) // Green
    }

    @JvmStatic
    fun ledOnOffColorC(on: Boolean): java.awt.Color {
        return if (!on) java.awt.Color(0.7f, 0f, 0f) // Red
        else java.awt.Color(0f, 0.7f, 0f) // Green
    }

    @JvmStatic
    fun drawLight(part: Obj3DPart?) {
        // TODO: Modern rendering
    }

    fun drawLight(part: Obj3DPart?, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int, r: Float, g: Float, b: Float, a: Float) {
        if (part == null) return
        // Use a lightmap-ignoring render type (like lightning or beacon beam, or custom)
        // For now, let's try to use the part's drawColored but with full brightness
        // We need a custom render type that ignores lightmap (always bright)
        
        // For now, just draw it normally but with max lightmap
        // 15728880 is max light (15 sky, 15 block)
        val maxLight = 15728880 
        
        // We might want to use a translucent render type if it's a light glow
        val consumer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.lightning()) // Lightning is glowing? Or maybe beaconBeam?
        // Or just use the part's texture but override light
        
        // Let's use the part's texture but force max light
        val texture = part.getTextureResource() ?: net.minecraft.resources.ResourceLocation("eln", "textures/missing.png")
        val renderType = net.minecraft.client.renderer.RenderType.entityTranslucent(texture) // Translucent handles alpha
        val consumerNormal = bufferSource.getBuffer(renderType)
        
        // We can't easily force "ignore lightmap" with standard render types without custom shaders or using specific types like 'eyes'
        // 'entityCutoutNoCull' respects lightmap.
        // 'beaconBeam' ignores lightmap but might have weird blending.
        
        // Let's just pass maxLight to the draw call
        part.drawColored(poseStack, consumerNormal, maxLight, packedOverlay, 0f, 0f, (r*255).toInt(), (g*255).toInt(), (b*255).toInt(), (a*255).toInt())
    }

    fun drawLight(part: Obj3DPart?, poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, packedOverlay: Int) {
        drawLight(part, poseStack, bufferSource, packedLight, packedOverlay, 1f, 1f, 1f, 1f)
    }

    @JvmStatic
    fun drawLightNoBind(part: Obj3DPart?) {
        // TODO: Modern rendering
    }

    @JvmStatic
    fun drawGuiBackground(ressource: ResourceLocation?, guiScreen: Screen, xSize: Int, ySize: Int) {
        // TODO: Modern rendering with GuiGraphics
    }

    fun drawLight(part: Obj3DPart?, angle: Float, x: Float, y: Float, z: Float) {
        // TODO: Modern rendering
    }

    @JvmStatic
    fun glDefaultColor() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    @JvmStatic
    fun drawEntityItem(entityItem: ItemEntity?, x: Double, y: Double, z: Double, roty: Float, scale: Float) {
        // TODO: Modern rendering
    }

    @JvmStatic
    fun drawConnectionPinSixNode(d: Float, w: Float, h: Float) {
        // TODO: Modern rendering
    }

    @JvmStatic
    fun drawConnectionPinSixNode(front: mods.eln.misc.LRDU, dList: FloatArray, w: Float, h: Float) {
        // TODO: Modern rendering
    }

    fun mc(): Minecraft {
        return Minecraft.getInstance()
    }

    fun guiScale() {
        // TODO: Modern rendering
    }

    @JvmStatic
    fun drawItemStack(par1ItemStack: net.minecraft.world.item.ItemStack?, x: Int, y: Int, par4Str: String?, gui: Boolean) {
        // TODO: Modern rendering
    }

    fun clientDistanceTo(e: Entity?): Double {
        val player = Minecraft.getInstance().player ?: return 100000000.0
        if (e == null) return 100000000.0
        return sqrt(player.distanceToSqr(e))
    }

    @JvmStatic
    fun clientDistanceTo(t: TransparentNodeBlockEntity?): Double {
        val player = Minecraft.getInstance().player ?: return 100000000.0
        if (t == null) return 100000000.0
        val pos = t.blockPos
        return sqrt(player.distanceToSqr(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()))
    }

    fun getLight(w: Level, x: Int, y: Int, z: Int): Int {
        return w.getMaxLocalRawBrightness(BlockPos(x, y, z))
    }

    @JvmStatic
    fun disableDepthTest() {
        RenderSystem.disableDepthTest()
    }

    @JvmStatic
    fun enableDepthTest() {
        RenderSystem.enableDepthTest()
    }

    @JvmStatic
    fun sendPacketToServer(bos: java.io.ByteArrayOutputStream) {
        ElnNetwork.sendToServer(ElnPacket(bos.toByteArray()))
    }

    val glListsAllocated = HashSet<Int>()
    @JvmStatic
    fun glGenListsSafe(): Int {
        return 0
    }

    @JvmStatic
    fun glDeleteListsSafe(id: Int) {
    }

    @JvmStatic
    fun glDeleteListsAllSafe() {
        glListsAllocated.clear()
    }

    @JvmStatic
    fun showItemTooltip(details: List<String>, realismDetails: List<String>, realisticEnum: RealisticEnum?, dst: MutableList<String>) {
        if (realisticEnum != null)
            dst.add("§r${realisticEnum.color}${realisticEnum.name}§r")
        if (details.isNotEmpty()) {
            if (isShiftHeld()) {
                dst.addAll(details)
            } else {
                dst.add("§F§o${tr("Hold [shift] for details")}")
            }
        }
        if (realismDetails.isNotEmpty()) {
            if (isControlHeld()) {
                dst.addAll(realismDetails)
            } else {
                if (realisticEnum != null) {
                    if (realismDetails.isNotEmpty()) {
                        dst.add("§F§o${tr("Hold [ctrl] for realism details")}")
                    }
                }
            }
        }
    }

    private fun isShiftHeld(): Boolean {
        return Screen.hasShiftDown()
    }

    private fun isControlHeld(): Boolean {
        return Screen.hasControlDown()
    }

    @JvmStatic
    fun getWeather(world: Level): Double {
        if (world.isThundering) return 1.0
        return if (world.isRaining) 0.5 else 0.0
    }

    @JvmStatic
    fun setGlColorFromDye(dyeColor: Int) {
        val color = net.minecraft.world.item.DyeColor.byId(dyeColor).textureDiffuseColors
        RenderSystem.setShaderColor(color[0], color[1], color[2], 1f)
    }

    @JvmStatic
    fun setGlColorFromDye(dyeColor: Int, alpha: Float) {
        val color = net.minecraft.world.item.DyeColor.byId(dyeColor).textureDiffuseColors
        RenderSystem.setShaderColor(color[0], color[1], color[2], alpha)
    }

    @JvmStatic
    fun setGlColorFromDye(dyeColor: Int, brightness: Float, alpha: Float) {
        val color = net.minecraft.world.item.DyeColor.byId(dyeColor).textureDiffuseColors
        RenderSystem.setShaderColor(color[0] * brightness, color[1] * brightness, color[2] * brightness, alpha)
    }

    @JvmStatic
    fun getDyeColor(dyeColor: Int): FloatArray {
        return net.minecraft.world.item.DyeColor.byId(dyeColor).textureDiffuseColors
    }

    // Helper for translation
    fun tr(s: String): String {
        return s // Placeholder
    }
}

// Stub classes if missing
class TransparentNodeBlockEntity(val blockPos: BlockPos)


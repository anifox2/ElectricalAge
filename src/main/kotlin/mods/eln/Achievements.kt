package mods.eln

import mods.eln.i18n.I18N
import mods.eln.i18n.I18N.tr
import net.minecraft.world.item.Items
import net.minecraft.resources.ResourceLocation
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.DisplayInfo
import net.minecraft.advancements.FrameType
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Blocks

/**
 * Legacy achievement system stub.
 *
 * In modern Minecraft (1.12+), the old Achievement/AchievementPage system was
 * replaced by JSON-defined Advancements. Forge 1.20 expects you to define
 * advancements as data files under data/<modid>/advancements rather than
 * Java code.
 *
 * This object is kept as a thin façade so existing code that calls
 * Achievements.init() still compiles, but it no longer tries to register
 * legacy achievements.
 */
object Achievements {

    /**
     * Dummy fields preserved so other code that referenced them will still compile.
     * In a proper 1.20 port, you should create real advancements as JSON data and,
     * if needed, look them up via the server's AdvancementManager.
     */
    @JvmField
    var openGuideId: ResourceLocation? = null

    @JvmField
    var craft50VMaceratorId: ResourceLocation? = null

    @JvmStatic
    fun init() {
        // In 1.20, advancements are JSON data. This method can be used as a hook
        // for any runtime checks or logging, but registration itself happens via
        // data packs, not code.
        //
        // We keep some symbolic IDs here so other parts of the mod can refer to
        // them if needed.
        openGuideId = ResourceLocation(Eln.MODID, "open_guide")
        craft50VMaceratorId = ResourceLocation(Eln.MODID, "craft_50v_macerator")

        // Optional: you can log or ensure translation keys exist.
        //I18N.TR_DESC(I18N.Type.ACHIEVEMENT, "open_guide")
        //I18N.TR_DESC(I18N.Type.ACHIEVEMENT, "craft_50v_macerator")
    }
}

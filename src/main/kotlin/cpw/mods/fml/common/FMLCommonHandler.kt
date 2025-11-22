package cpw.mods.fml.common

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.loading.FMLEnvironment

class FMLCommonHandler {
    val effectiveSide: Side
        get() = if (FMLEnvironment.dist == Dist.CLIENT) Side.CLIENT else Side.SERVER

    companion object {
        private val INSTANCE = FMLCommonHandler()
        fun instance(): FMLCommonHandler = INSTANCE
    }
}

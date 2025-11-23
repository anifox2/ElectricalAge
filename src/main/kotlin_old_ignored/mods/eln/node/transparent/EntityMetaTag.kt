package mods.eln.node.transparent

/**
 * Used to differentiate between subclasses of TransparentNodeBlockEntity, so that
 * our TEs can implement different interfaces depending on what functionality
 * they have.
 */
enum class EntityMetaTag(val meta: Int, val cls: Class<*>) {
    Fluid(1, TransparentNodeBlockEntityWithFluid::class.java),
    Basic(3, TransparentNodeBlockEntity::class.java); // 3, because this is the default value used in pre-metatag worlds
}

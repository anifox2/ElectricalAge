package mods.eln.item

import org.lwjgl.opengl.GL11

class EntitySensorFilterDescriptor(
    name: String,
    @JvmField var entityClass: Class<*>,
    @JvmField var r: Float,
    @JvmField var g: Float,
    @JvmField var b: Float
) : GenericItemUsingDamageDescriptorUpgrade(name) {

    fun glColor() {
        GL11.glColor3f(r, g, b)
    }

    fun glColor(intensity: Float) {
        GL11.glColor3f(r * intensity, g * intensity, b * intensity)
    }

    fun glInverseColor(intensity: Float) {
        GL11.glColor3f(1.0f - r * intensity, 1.0f - g * intensity, 1.0f - b * intensity)
    }
}

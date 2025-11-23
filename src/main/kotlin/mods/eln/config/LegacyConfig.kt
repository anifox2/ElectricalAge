package mods.eln.config

class LegacyConfig {
    fun load() {
        // No-op in 1.20.1, handled by Forge
    }

    fun save() {
        // No-op in 1.20.1, handled by Forge
    }

    fun hasKey(category: String, key: String): Boolean {
        // Stub: always return false or implement if needed for migration
        return false
    }

    fun renameProperty(category: String, oldKey: String, newKey: String) {
        // Stub
    }

    operator fun get(category: String, key: String, default: Boolean, comment: String? = null, min: Double? = null, max: Double? = null): LegacyProperty {
        return LegacyProperty(default)
    }

    operator fun get(category: String, key: String, default: Int, comment: String? = null, min: Double? = null, max: Double? = null): LegacyProperty {
        return LegacyProperty(default)
    }

    operator fun get(category: String, key: String, default: Double, comment: String? = null, min: Double? = null, max: Double? = null): LegacyProperty {
        return LegacyProperty(default)
    }

    operator fun get(category: String, key: String, default: String, comment: String? = null): LegacyProperty {
        return LegacyProperty(default)
    }
}

class LegacyProperty(private val defaultValue: Any) {
    fun getBoolean(default: Boolean): Boolean {
        return defaultValue as? Boolean ?: default
    }

    fun getInt(default: Int): Int {
        return (defaultValue as? Number)?.toInt() ?: default
    }

    fun getDouble(default: Double): Double {
        return (defaultValue as? Number)?.toDouble() ?: default
    }

    val boolean: Boolean
        get() = defaultValue as? Boolean ?: false

    val int: Int
        get() = (defaultValue as? Number)?.toInt() ?: 0

    val double: Double
        get() = (defaultValue as? Number)?.toDouble() ?: 0.0

    val string: String
        get() = defaultValue.toString()

    fun set(value: String) {
        // Stub
    }
}

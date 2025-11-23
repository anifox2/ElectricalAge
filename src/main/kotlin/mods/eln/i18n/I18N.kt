package mods.eln.i18n

object I18N {
    @JvmStatic
    fun tr(text: String, vararg objects: Any): String {
            // Placeholder implementation
            return text
        }

        @JvmStatic
        fun TR(text: String): String {
            return encodeLangKey(text)
        }

        @JvmStatic
        fun TR_NAME(type: Type, text: String): String {
            if (type.encodeAtRuntime) {
                return type.prefix + encodeLangKey(text) + ".name"
            } else {
                return text
            }
        }

        @JvmStatic
        fun getCurrentLanguage(): String {
            return try {
                net.minecraft.client.Minecraft.getInstance().languageManager.selected
            } catch (e: Throwable) {
                "en_us"
            }
        }

        @JvmStatic
        @JvmOverloads
        fun encodeLangKey(key: String, replaceWhitespaces: Boolean = true): String {
            var k = key
            if (replaceWhitespaces) {
                k = k.replace(' ', '_')
            }
            return k.replace("=", "\\=")
                .replace(":", "\\:")
                .replace("\n", "\\n")
                .replace("/", "_")
        }

    enum class Type(val prefix: String, val encodeAtRuntime: Boolean, val replaceWhitespacesInFile: Boolean) {
        NONE("", false, true),
        ITEM("item.", false, false),
        TILE("tile.", false, false),
        ACHIEVEMENT("achievement.", true, true),
        ENTITY("entity.", false, false),
        DEATH_ATTACK("death.attack.", false, false),
        ITEM_GROUP("itemGroup.", false, false),
        CONTAINER("container.", false, false),
        BLOCK("block.", false, false),
        SIX_NODE("eln.sixnode.", false, true),
        NODE("eln.node.", false, true)
    }
}

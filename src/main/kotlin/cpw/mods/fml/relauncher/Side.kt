package cpw.mods.fml.relauncher

enum class Side {
    CLIENT, SERVER;

    val isClient: Boolean
        get() = this == CLIENT
}

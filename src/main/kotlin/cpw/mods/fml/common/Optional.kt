package cpw.mods.fml.common

annotation class Optional {
    annotation class Interface(val iface: String, val modid: String, val striprefs: Boolean = false)
    annotation class InterfaceList(val value: Array<Interface>)
    annotation class Method(val modid: String)
}

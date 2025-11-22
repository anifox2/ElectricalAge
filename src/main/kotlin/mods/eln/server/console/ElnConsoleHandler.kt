package mods.eln.server.console

import mods.eln.misc.FC
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import java.lang.Exception
import java.util.*

val ElnConsoleCommandList = mutableListOf<IConsoleCommand>()

class ElnConsoleCommands {

    init {
        ElnConsoleCommandList.addAll(listOf(
            ElnLsCommand(),
            ElnAboutCommand(),
            ElnVersionCommand(),
            ElnCablePaceCommand(),
            ElnAgingCommand(),
            ElnBatteryAgingCommand(),
            ElnLampAgingCommand(),
            ElnHeatFurnaceFuelCommand(),
            ElnNewWindDirectionCommand(),
            ElnRegenOreQueueCommand(),
            ElnLampsKillMonstersCommand(),
            ElnMatrixCommand(),
            ElnManCommand(),
            ElnWailaEasyModeCommand(),
            ElnDebugCommand(),
            ElnExplosionsCommand(),
            ElnIconsCommand()
        ))
    }

    companion object {
        fun cprint(ics: CommandSourceStack, text: String, indent: Int = 0) {
            printIndented(text, indent).forEach {
                ics.sendSystemMessage(Component.literal(it))
            }
        }

        fun cprint(ics: CommandSourceStack, text: String, url: String) {
            val msg = Component.literal(FC.BRIGHT_GREY + text).withStyle { 
                it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
            }
            ics.sendSystemMessage(msg)
        }

        fun printIndented(text: String, indent: Int): List<String> {
            val lineLength = 60 - indent * 2
            val list = mutableListOf<String>()
            val finalLine = text
                .split(' ')
                .fold("", { acc, next ->
                    if (acc.length + next.length > lineLength) {
                        list.add(acc)
                        next
                    } else {
                        "$acc $next"
                    }
                })
            list.add(finalLine)
            var mostRecentColor = '7' // default color is light gray
            val list2 = list.map  {
                line ->
                val lastLine = "§$mostRecentColor$line"
                if ("§" in line) {
                    mostRecentColor = line[line.lastIndexOf("§") + 1]
                }
                lastLine
            }
            val whitespace = (0 until indent * 2).joinToString("") { " " }
            return list2.map{"$whitespace$it"}
        }

        fun getArgBool(ics: CommandSourceStack, arg: String): Boolean? {
            val lowerArg = arg.lowercase()
            return if (lowerArg.isEmpty()) {
                cprint(ics, "Error: Empty argument.", indent = 1)
                null
            }else if (lowerArg == "0" || lowerArg == "false" || lowerArg == "no" || lowerArg == "disabled") {
                false
            } else if (lowerArg == "1" || lowerArg == "true" || lowerArg == "yes" || lowerArg == "enabled") {
                true
            } else {
                cprint(ics, "Error: Expected (true/false), got $arg",  indent = 1)
                null
            }
        }

        fun boolToStr(value: Boolean): String {
            return if (value) "Enabled" else "Disabled"
        }
    }

    fun processCommand(ics: CommandSourceStack, args: Array<out String>) {
        if (args.isEmpty()) {
            cprint(ics,"${FC.DARK_CYAN}Electrical Age Console, run /eln ls for commands${FC.BRIGHT_GREY }")
            return
        }
        val permissions = determinePermissionsList(ics)
        val command = ElnConsoleCommandList.filter { it.name.equals(args[0], ignoreCase = true) }
        if (command.isEmpty()) {
            cprint(ics,"${FC.DARK_CYAN}Command not found, run /eln ls for commands${FC.BRIGHT_GREY }")
            return
        }
        cprint(ics, "${FC.DARK_CYAN}${ics.textName} $${FC.DARK_YELLOW} /eln ${args.joinToString(" ")}")
        val canRun = permissions.any { command[0].requiredPermission().contains(it) }
        if (canRun) {
            command[0].runCommand(ics, args.toList().drop(1))
        } else {
            cprint(ics, "${FC.DARK_CYAN}You do not have permission to run that command. " +
                "You need to have one of the following: ${command[0].requiredPermission()}${FC.BRIGHT_GREY }")
        }
    }

    fun determinePermissionsList(ics: CommandSourceStack): List<UserPermission> {
        var creative = false
        var singlePlayer = false
        var isOperator = false
        val player = ics.player
        val console = player == null
        if (!console) {
            creative = player!!.isCreative
            singlePlayer = ics.server.isSingleplayer
            isOperator = ics.server.playerList.isOp(player.gameProfile)
        }
        val playerPerms = mutableListOf<UserPermission>()
        if (creative)
            playerPerms.add(UserPermission.IS_CREATIVE)
        if (console) {
            playerPerms.add(UserPermission.IS_CONSOLE)
            playerPerms.add(UserPermission.IS_OPERATOR)
        }
        if (isOperator)
            playerPerms.add(UserPermission.IS_OPERATOR)
        if (singlePlayer)
            playerPerms.add(UserPermission.IS_OPERATOR)
        return playerPerms.toList()
    }
}

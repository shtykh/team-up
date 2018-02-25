package shtykh.teamup.controller.state

import com.github.ivan_osipov.clabo.state.chat.ChatState
import shtykh.teamup.controller.Command
import shtykh.teamup.controller.TeamUpChatContext

abstract class TeamUpState(open val message: String, context: TeamUpChatContext, chatId: String) : ChatState<TeamUpChatContext>(chatId, context) {
    constructor(message: String, prev: TeamUpState) : this(message, prev.context, prev.chatId)

    fun next(command: String?, parameter: String?): TeamUpState {
        val internalCommand = Command(command.orEmpty())
        if(! isAllowed(internalCommand)) return Start("${context.adressent?.username} is not allowed to perform /$command", context, chatId)
        return nextOrNull(internalCommand, parameter)
                ?: Start("Invalid command \"$command\" for state ${this::class.java.simpleName}", context, chatId)
    }

    fun answer(): String {
        val commandString = getCommandNames()
            .takeIf { it.isNotEmpty() }
            ?.map { Command.get(it, Command.Companion::forHelpMapper) }
            ?.reduce { fi, se -> "$fi\n$se" }
            .orEmpty()
        return "$message\n" + commandString
    }

    abstract fun isAllowed(command: Command): Boolean

    abstract fun getCommandNames(): List<String>

    abstract fun nextOrNull(command: Command, parameter: String?): TeamUpState?

    fun <T> findObject(key: String?,
                       objectByKey: (String) -> T?,
                       stateByObject: (T) -> TeamUpState = { Start("Illegal call for ${this.javaClass.simpleName} as $it", context, chatId) },
                       stateByParameter: (String?) -> TeamUpState = { objectNotFound(it) })
            : TeamUpState {
        val objectName = key ?: ""
        val obj = objectByKey(objectName)
        return if (obj != null) {
            stateByObject(obj)
        } else {
            stateByParameter(key)
        }
    }

    fun objectNotFound(parameter: String?) = Start("Illegal parameter for ${this.javaClass.simpleName} as $parameter", context, chatId)
}

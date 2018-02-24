package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.state.chat.ChatContext
import com.github.ivan_osipov.clabo.state.chat.ChatState

abstract class TeamUpState(open val message: String, context: ChatContext, chatId: String) : ChatState<ChatContext>(chatId, context) {
    constructor(message: String, prev: TeamUpState) : this(message, prev.context, prev.chatId)

    fun next(command: String?, parameter: String?): TeamUpState {
        val internalCommand = command ?: ""
        return nextOrNull(internalCommand, parameter)
                ?: Start("Invalid command \"$command\" for state ${this::class.java.simpleName}", context, chatId)
    }

    fun answer(): String {
        val commandString = getCommands()
                .takeIf { it.isNotEmpty() }
                ?.map { Commands.get(it, Commands::forStart) }
                ?.reduce { fi, se -> "$fi\n$se" }
                .orEmpty()
        return "$message\n" + commandString
    }

    abstract fun getCommands(): List<String>

    abstract fun nextOrNull(command: String, parameter: String?): TeamUpState?

    inline fun <reified T> forCommand(expectedCommand: String,
                                      command: String,
                                      parameter: String?,
                                      supplier: (String) -> T?,
                                      successState: (T) -> TeamUpState,
                                      stateByParameterSupplier: (String?) -> TeamUpState): TeamUpState? {
        return if (expectedCommand.equals(command, ignoreCase = true)) {
            findObject(parameter, supplier, successState) {
                stateByParameterSupplier.invoke(it)
            }
        } else null
    }

    inline fun <reified T> forCommand(expectedCommand: String,
                                      command: String,
                                      parameter: String?,
                                      supplier: (String) -> T?,
                                      successState: (T) -> TeamUpState): TeamUpState? {
        return forCommand(expectedCommand, command, parameter, supplier, successState) {
            Start("No such ${T::class.java.simpleName} as $it", context, chatId)
        }
    }

    fun defaultState(parameter: String?): TeamUpState {
        return Start("${this.javaClass.simpleName} failed on parameter $parameter", context, chatId)
    }

    inline fun <reified T> findObject(key: String?,
                                           objectSupplier: (String) -> T?,
                                           stateByObjectSupplier: (T) -> TeamUpState,
                                           stateByParameterSupplier: (String?) -> TeamUpState = {Start("Illegal parameter for ${this.javaClass.simpleName} as $it", context, chatId)})
            : TeamUpState {
        val objectName = key ?: ""
        val obj = objectSupplier.invoke(objectName)
        return if (obj != null) {
            stateByObjectSupplier.invoke(obj)
        } else {
            stateByParameterSupplier.invoke(key)
        }
    }

    fun objectNotFound(): (String?) -> Start = {it -> Start("Illegal parameter for ${this.javaClass.simpleName} as $it", context, chatId)}
}

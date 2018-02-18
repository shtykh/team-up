package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.state.chat.ChatContext
import com.github.ivan_osipov.clabo.state.chat.ChatState

abstract class TeamUpState(open val message: String, context: ChatContext, chatId: String) : ChatState<ChatContext>(chatId, context) {
    fun next(command: String?, parameter: String?): TeamUpState {
        val internalCommand = command ?: ""
        return nextOrNull(internalCommand, parameter)
                ?: Start("Invalid command \"$command\" for state ${this::class.java.simpleName}", context, chatId)
    }

    fun answer(): String {
        return "$message\n" +
                getCommands().map { Commands.get(it.toString(), Commands::forStart) }
                        .reduce { fi, se -> "$fi\n$se" }
    }

    open fun getCommands(): List<Any> {
        return commands.keys().toList()
    }


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
                                      stateByParameterSupplier: (String?) -> TeamUpState): TeamUpState {
        val objectName = key ?: ""
        val obj = objectSupplier.invoke(objectName)
        return if (obj != null) {
            stateByObjectSupplier.invoke(obj)
        } else {
            stateByParameterSupplier.invoke(key)
        }
    }
}

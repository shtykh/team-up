package shtykh.teamup.controller.state

import com.github.ivan_osipov.clabo.api.model.Message
import com.github.ivan_osipov.clabo.state.chat.ChatState
import shtykh.teamup.controller.Command
import shtykh.teamup.controller.TeamUpChatContext
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.event.Event
import shtykh.teamup.domain.team.Team
import shtykh.teamup.domain.team.util.Jsonable

abstract class TeamUpState(open val message: String, context: TeamUpChatContext, chatId: String) : ChatState<TeamUpChatContext>(chatId, context) {
    constructor(message: String, prev: TeamUpState) : this(message, prev.context, prev.chatId)

    fun next(commandString: String, message: Message): TeamUpState {
        val command = Command(commandString)
        val parameter = parameter(message, commandString)
        return when(command) {
            Command("start") -> Start("${this.javaClass.simpleName} is aborted", context, chatId)
            else -> if(!isAllowed(command)) {
                Start("${context.adressent?.username} is not allowed to perform /$commandString", context, chatId)
            } else {
                nextOrNull(command, parameter.orEmpty())
                    ?: ErrorState("Invalid command \"$commandString\" for state ${this::class.java.simpleName}", this)
            }
        }
    }

    private fun parameter(message: Message, command: String?) = if (command.orEmpty().isBlank()) {
        message.text
    } else {
        message.text?.removePrefix("/$command")?.removePrefix(" ")
    }

    fun answer(): String {
        val commandString = getCommandNames()
            .takeIf { it.isNotEmpty() }
            ?.map { Command.get(it, Command.Companion::forHelpMapper) }
            ?.reduce { fi, se -> "$fi\n$se" }
            .orEmpty()
        return "${this.javaClass.simpleName}: $message\n" + commandString
    }

    abstract fun isAllowed(command: Command): Boolean

    abstract fun getCommandNames(): List<String>

    abstract fun nextOrNull(command: Command, parameter: String): TeamUpState?

    fun <T> findObject(key: String,
                       objectByKey: (String) -> T?,
                       stateByObject: (T) -> TeamUpState = { Start("Illegal call for ${this.javaClass.simpleName} as $it", context, chatId) },
                       stateByParameter: (String?) -> TeamUpState = { objectNotFound(it) })
        : TeamUpState {
        val obj = objectByKey(key)
        return if (obj != null) {
            stateByObject(obj)
        } else {
            stateByParameter(key)
        }
    }

    fun objectNotFound(parameter: String?) = ErrorState("Illegal parameter for ${this.javaClass.simpleName} as $parameter", this)

    fun error(command: Command = Command(), vararg parameter: Any?) =
        ErrorState("Something went wrong! Was trying ${command.value} from ${this.javaClass} " +
            "with ${parameter.map { it.toString() }.reduce { acc, s -> acc + ", " + s }}", this)
}

open class Start(override val message: String, context: TeamUpChatContext, chatId: String) : TeamUpState(message, context, chatId) {
    override fun isAllowed(command: Command) = true

    override fun nextOrNull(command: Command, parameter: String): TeamUpState? {
        return when (command) {
            Command("team") -> findObject(parameter, { Team.get(it) }, { TeamChosen(it, this) }) {
                ChooseTeam(parameter, this)
            }
            Command("event") -> findObject(parameter, { Event.get(it) }, { EventChosen(it, this) }) {
                ChooseEvent(parameter, this)
            }
            Command("person") -> findObject(parameter, { Person.get(it) }, { PersonChosen(it, this) }) {
                ChoosePerson(parameter, this)
            }
            else -> null
        }
    }

    override fun getCommandNames(): List<String> {
        return listOf("team", "event", "person")
    }
}

abstract class MessageReceiverState(message: String, prev: TeamUpState) :
    TeamUpState(message, prev) {

    override fun nextOrNull(command: Command, parameter: String): TeamUpState? {
        return if (command == Command()) successState(parameter) else forCommand(command, parameter)
    }

    open fun forCommand(command: Command, parameter: String?): TeamUpState? = null

    abstract fun successState(parameter: String): TeamUpState?
}

class EditJson<out T: Jsonable>(val obj: T, prev: TeamUpState) : MessageReceiverState("Now it's like this: \n${obj.toJson()}\n" + "Send me it in json:", prev) {

    override fun isAllowed(command: Command): Boolean {
        return true
    }

    override fun getCommandNames(): List<String> = listOf()

    override fun successState(parameter: String): TeamUpState {
        return when (obj) {
            is Person -> {
                try {
                    val newObj: Person = Jsonable.fromJson(parameter)
                    newObj.save()
                    PersonChosen(newObj, this)
                } catch (ex: Exception) {
                    error(parameter = *arrayOf(parameter, ex.message))
                }
            }
            else -> error(parameter = *arrayOf(parameter))
        }
    }
}

class ErrorState(msg: String = "", prev: TeamUpState) : Start(msg, prev.context, prev.chatId)

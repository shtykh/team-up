package shtykh.teamup.controller.state

import shtykh.teamup.controller.Command
import shtykh.teamup.controller.TeamUpChatContext
import shtykh.teamup.domain.event.Event
import shtykh.teamup.domain.team.Team

open class Start(override val message: String, context: TeamUpChatContext, chatId: String) : TeamUpState(message, context, chatId) {
    override fun isAllowed(command: Command) = true

    override fun nextOrNull(command: Command, parameter: String?): TeamUpState? {
        return when (command) {
            Command("team") -> findObject(parameter, Team.Companion::get, { TeamChosen(it, this) }) {
                ChooseTeam(parameter, this)
            }
            Command("event") -> findObject(parameter, Event.Companion::get, { EventChosen(it, this) }) {
                ChooseEvent(parameter, this)
            }
            else -> {
                IllegalParameter(command, this)
            }
        }
    }

    override fun getCommandNames(): List<String> {
        return listOf("team", "event")
    }
}

abstract class MessageReceiverState(message: String, prev: TeamUpState) :
    TeamUpState(message, prev) {

    override fun nextOrNull(command: Command, parameter: String?): TeamUpState? {
        return if (command == Command("")) successState(parameter) else forCommand(command, parameter)
    }

    open fun forCommand(command: Command, parameter: String?): TeamUpState? = null

    abstract fun successState(parameter: String?): TeamUpState
}

class IllegalParameter(parameter: Command, prev: TeamUpState) : Start("Illegal parameter for state ${prev.javaClass.simpleName}: $parameter", prev.context, prev.chatId)

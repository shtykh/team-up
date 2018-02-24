package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.api.input.toJson
import com.github.ivan_osipov.clabo.state.chat.ChatContext
import shtykh.teamup.domain.Party
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.event.Event
import shtykh.teamup.domain.team.Team
import java.util.*


open class Start(override val message: String, context: ChatContext, chatId: String) : TeamUpState(message, context, chatId) {
    override fun nextOrNull(command: String, parameter: String?): TeamUpState? {
        return when(command) {
            "team" -> findObject(parameter, Team.Companion::get, ::teamChosen) {
                ChooseTeam(parameter, this)
            }
            "event" -> findObject(parameter, Event.Companion::get, ::eventChosen) {
                ChooseEvent(parameter, this)
            }
            else -> {
                IllegalParameter(command, this)
            }
        }
    }

    fun teamChosen(it: Team) = TeamChosen(it, this)

    fun eventChosen(it: Event) = EventChosen(it, this)

    override fun getCommands(): List<String> {
        return listOf("team", "event")
    }
}

class IllegalParameter(parameter: String?, prev: TeamUpState): Start("Illegal parameter for state ${prev.javaClass.simpleName}: $parameter", prev.context, prev.chatId)

class ChooseEvent(badName: String?, prev: TeamUpState) :
        MessageReceiverState("Give me event name better than \"${badName.orEmpty()}\"", prev) {
    override fun getCommands(): List<String> = emptyList()

    override fun successState(parameter: String?): TeamUpState {
        return findObject(parameter, Event.Companion::get, this::eventChosen) {
            ChooseTeam(parameter, this)
        }
    }

    fun eventChosen(it: Event) = EventChosen(it, this)
}

class ChooseTeam(badName: String?, prev: TeamUpState) :
        MessageReceiverState("Give me team name better than \"${badName.orEmpty()}\"", prev) {
    override fun getCommands(): List<String> = emptyList()

    override fun successState(parameter: String?): TeamUpState {
        return findObject(parameter, Team.Companion::get, this::teamChosen) {
            ChooseTeam(parameter, this)
        }
    }

    fun teamChosen(it: Team) = TeamChosen(it, this)
}


abstract class PartyChosen<T : Party<T>>(private val party: Party<T>, answer: String, prev: TeamUpState) :
        TeamUpState(answer, prev) {
    override fun nextOrNull(command: String, parameter: String?): TeamUpState? {
        return forCommand("hire", command, parameter, Person.Companion::get) {
            hire(it)
        } ?: forCommand("fire", command, parameter, Person.Companion::get) {
            fire(it)
        } ?: forCommand("setName", command, parameter, { parameter }, {
            setName(it)
        }) {
            Rename(party, this)
        }
    }

    fun hire(person: Person): TeamUpState {
        return instance(party.instance() hire person)
    }

    fun fire(person: Person): TeamUpState {
        return instance(party.instance() fire person)
    }

    fun setName(name: String): TeamUpState {
        party.name = name
        return instance(party.instance())
    }

    override fun getCommands(): List<String> {
        return Arrays.asList("hire, fire, setName, help")
    }

    abstract fun instance(party: T): PartyChosen<*>
}

class TeamChosen(val team: Team, val prev: TeamUpState) :
        PartyChosen<Team>(team, "Team chosen: ${team.toJson()}", prev) {
    override fun instance(party: Team): PartyChosen<*> {
        return TeamChosen(party, prev)
    }

    override fun nextOrNull(command: String, parameter: String?): TeamUpState? {
        return super.nextOrNull(command, parameter)
                ?: forCommand("hirelegio", command, parameter, { Person.get(it) }, { hirelegio(it) })
                ?: forCommand("firelegio", command, parameter, { Person.get(it) }, { firelegio(it) })
    }

    fun hirelegio(person: Person): TeamUpState {
        team.legio hire person
        return TeamChosen(team, this)
    }

    fun firelegio(person: Person): TeamUpState {
        team.legio fire person
        return TeamChosen(team, this)
    }

    override fun getCommands(): List<String> {
        return Arrays.asList("hire", "fire", "hireLegio", "fireLegio", "setName", "help")
    }
}

class EventChosen(val event: Event, val prev: TeamUpState) :
        PartyChosen<Event>(event, "Event chosen: ${event.toJson()}", prev) {
    override fun instance(party: Event): PartyChosen<*> {
        return EventChosen(event, prev)
    }
}

abstract class MessageReceiverState(message: String, prev: TeamUpState) :
        TeamUpState(message, prev) {

    override fun nextOrNull(command: String, parameter: String?): TeamUpState? {
        return if (command == "") successState(parameter) else null
    }

    abstract fun successState(parameter: String?): TeamUpState
}

class Rename(val party: Party<*>, prev: TeamUpState) :
        MessageReceiverState("Rename ${party.name}", prev) {
    override fun getCommands(): List<String> = emptyList()

    override fun successState(parameter: String?): TeamUpState {
        val oldName = party.name
        party.name = parameter ?: oldName
        return Start("$oldName was renamed to ${party.name}", context, chatId)
    }
}

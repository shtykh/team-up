package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.state.chat.ChatContext
import com.github.ivan_osipov.clabo.utils.ChatId
import shtykh.teamup.domain.Party
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.event.Event
import shtykh.teamup.domain.team.Team
import java.util.*


class Start(override val message: String, context: ChatContext, chatId: String) : TeamUpState(message, context, chatId) {
    override fun nextOrNull(command: String, parameter: String?): TeamUpState? {
        return forCommand("team", command, parameter, Team.Companion::get, ::teamChosen) {
            ChooseTeam(parameter, context, chatId)
        } ?: forCommand("event", command, parameter, Event.Companion::get, ::eventChosen)
    }

    fun teamChosen(it: Team) = TeamChosen(it, context, chatId)

    fun eventChosen(it: Event) = EventChosen(it, context, chatId)


}

class ChooseTeam(badName: String?, context: ChatContext, chatId: ChatId) :
        MessageReceiverState("Give me team name better than \"${badName.orEmpty()}\"", context, chatId) {
    override fun successState(parameter: String?): TeamUpState {
        return findObject(parameter, Team.Companion::get, this::teamChosen) {
            ChooseTeam(parameter, context, chatId)
        }
    }

    fun teamChosen(it: Team) = TeamChosen(it, context, chatId)

}


abstract class PartyChosen<T : Party<T>>(private val party: Party<T>, answer: String, context: ChatContext, chatId: String) :
        TeamUpState(answer, context, chatId) {
    override fun nextOrNull(command: String, parameter: String?): TeamUpState? {
        return forCommand("hire", command, parameter, Person.Companion::get) {
            hire(it)
        } ?: forCommand("fire", command, parameter, Person.Companion::get) {
            fire(it)
        } ?: forCommand("setName", command, parameter, { parameter }, {
            setName(it)
        }) {
            Rename(party, context, chatId)
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

    override fun getCommands(): List<Any> {
        return Arrays.asList("hire, fire, setName, help")
    }

    abstract fun instance(party: T): PartyChosen<*>
}

class TeamChosen(val team: Team, context: ChatContext, chatId: String) :
        PartyChosen<Team>(team, "Team chosen: ${team.toJson()}", context, chatId) {
    override fun instance(party: Team): PartyChosen<*> {
        return TeamChosen(party, context, chatId)
    }

    override fun nextOrNull(command: String, parameter: String?): TeamUpState? {
        return super.nextOrNull(command, parameter)
                ?: forCommand("hirelegio", command, parameter, { Person.get(it) }, { hirelegio(it) })
                ?: forCommand("firelegio", command, parameter, { Person.get(it) }, { firelegio(it) })
    }

    fun hirelegio(person: Person): TeamUpState {
        team.legio hire person
        return TeamChosen(team, context, chatId)
    }

    fun firelegio(person: Person): TeamUpState {
        team.legio fire person
        return TeamChosen(team, context, chatId)
    }

    override fun getCommands(): List<Any> {
        return Arrays.asList("hire", "fire", "hireLegio", "fireLegio", "setName", "help")
    }
}

class EventChosen(val event: Event, context: ChatContext, chatId: String) :
        PartyChosen<Event>(event, "Event chosen: ${event.toJson()}", context, chatId) {
    override fun instance(party: Event): PartyChosen<*> {
        return EventChosen(event, context, chatId)
    }
}

abstract class MessageReceiverState(message: String, context: ChatContext, chatId: String) :
        TeamUpState(message, context, chatId) {

    override fun nextOrNull(command: String, parameter: String?): TeamUpState? {
        return if (command == "") successState(parameter) else null
    }

    abstract fun successState(parameter: String?): TeamUpState
}

class Rename(val party: Party<*>, context: ChatContext, chatId: String) :
        MessageReceiverState("Rename ${party.name}", context, chatId) {
    override fun successState(parameter: String?): TeamUpState {
        val oldName = party.name
        party.name = parameter ?: oldName
        return Start("$oldName was renamed to ${party.name}", context, chatId)
    }
}

package shtykh.teamup.controller

import shtykh.teamup.domain.Party
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.event.Event
import shtykh.teamup.domain.team.Team
import java.util.*

abstract class State(open val message: String) {
    fun next(command: String, parameter: String?): State {
        return nextInternal(command, parameter)
                ?: Start("Invalid command \"$command\" for state ${this::class.java.simpleName}")
    }

    fun answer(): String {
        return "$message\n" +
                getCommands().map {Commands.get(it.toString(), Commands::forStart)}
                        .reduce{fi, se -> "$fi\n$se" }
    }

    open fun getCommands(): List<Any> {
        return commands.keys().toList()
    }


    abstract fun nextInternal(command: String, parameter: String?): State?

    inline fun <reified T> forCommand(expectedCommand: String,
                                      command: String,
                                      parameter: String?,
                                      supplier: (String) -> T?,
                                      successState: (T) -> State): State? {
        return if (expectedCommand.equals(command, ignoreCase = true)) {
            findObject(parameter, supplier, successState)
        } else {
            null
        }
    }

    inline fun <reified T> findObject(key: String?,
                                      objectSupplier: (String) -> T?,
                                      stateSupplier: (T) -> State): State {
        val objectName = key ?: ""
        val team = objectSupplier.invoke(objectName)
        return if (team != null) {
            stateSupplier.invoke(team)
        } else {
            Start("No such ${T::class.java.simpleName} as \"$objectName\"")
        }
    }
}

class Start(override val message: String): State(message) {
    override fun nextInternal(command: String, parameter: String?): State? {
        return forCommand("team", command, parameter, {Team.get(it)}, {TeamChosen(it)})
                ?: forCommand("event", command, parameter, { Event.get(it)}, {EventChosen(it)})
    }


}


abstract class PartyChosen<T: Party<T>>(val party: Party<T>, answer: String): State(answer) {
    override fun nextInternal(command: String, parameter: String?): State? {
        return forCommand("hire", command, parameter, { Person.get(it) }, { hire(it) })
                ?: forCommand("fire", command, parameter, { Person.get(it) }, { fire(it) })
                ?: forCommand("setName", command, parameter, { parameter }, { setName(it) })
    }

    fun hire(person: Person): State {
        return instance(party.instance() hire person)
    }

    fun fire(person: Person): State {
        return instance(party.instance() fire person)
    }

    fun setName(name: String): State {
        party.name = name
        return instance(party.instance())
    }

    override fun getCommands(): List<Any> {
        return Arrays.asList("hire, fire, setName, help")
    }

    abstract fun instance(party: T): PartyChosen<*>
}

class TeamChosen(val team: Team): PartyChosen<Team>(team, "Team chosen: ${team.toJson()}") {
    override fun instance(party: Team): PartyChosen<*> {
        return TeamChosen(party)
    }

    override fun nextInternal(command: String, parameter: String?): State? {
        return super.nextInternal(command, parameter)
                ?: forCommand("hirelegio", command, parameter, {Person.get(it)}, { hirelegio(it)})
                ?: forCommand("firelegio", command, parameter, {Person.get(it)}, { firelegio(it)})
    }

    fun hirelegio(person: Person): State {
        team.legio hire person
        return TeamChosen(team)
    }

    fun firelegio(person: Person): State {
        team.legio fire person
        return TeamChosen(team)
    }

    override fun getCommands(): List<Any> {
        return Arrays.asList("hire, fire, hireLegio, fireLegio, setName, help")
    }
}

class EventChosen(val event: Event): PartyChosen<Event>(event, "Event chosen: ${event.toJson()}") {
    override fun instance(party: Event): PartyChosen<*> {
        return EventChosen(event)
    }
}

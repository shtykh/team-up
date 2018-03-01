package shtykh.teamup.controller.state

import shtykh.teamup.controller.Command
import shtykh.teamup.domain.event.Event
import shtykh.teamup.domain.team.Team

class ChooseEvent(badName: String?, prev: TeamUpState) :
    MessageReceiverState("Give me event name better than \"$badName\"", prev) {
    override fun isAllowed(command: Command) = true

    override fun getCommandNames(): List<String> = listOf(*Event.directory.cache.keys.toTypedArray())

    override fun successState(parameter: String): TeamUpState {
        return findObject(parameter, { Event.get(it) }, this::eventChosen) {
            ChooseEvent(parameter, this)
        }
    }

    fun eventChosen(it: Event) = EventChosen(it, this)
}

class CreateEvent(var team: Team, prev: TeamUpState) :
    MessageReceiverState("Choose a name for the new event", prev) {
    override fun isAllowed(command: Command) = true
    override fun getCommandNames(): List<String> = listOf()

    override fun successState(parameter: String): TeamUpState {
        return run {
            val adressent = context.adressent?.username ?: "Nobody"
            val event = Event(parameter, admin = adressent, teamId = team.id)
            event.save()
            EventChosen(event, this)
        }
    }
}

class EventChosen(event: Event, override val prev: TeamUpState) :
    PartyChosen<Event>(event, event.toJson(), prev) {
    override fun instance(party: Event): PartyChosen<*> {
        return EventChosen(party, prev)
    }
}

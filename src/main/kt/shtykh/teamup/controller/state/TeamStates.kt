package shtykh.teamup.controller.state

import shtykh.teamup.controller.Command
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.team.Team

class ChooseTeam(badName: String?, prev: TeamUpState) :
    MessageReceiverState("Give me team name better than \"$badName\"", prev) {
    override fun isAllowed(command: Command) = true

    override fun getCommandNames(): List<String> = listOf("newTeam", *Team.directory.cache.keys.toTypedArray())

    override fun forCommand(command: Command, parameter: String?): TeamUpState? {
        return when (command) {
            Command("newTeam") -> CreateTeam(this)
            else -> null
        }
    }

    override fun successState(parameter: String): TeamUpState {
        return findObject(parameter, Team.Companion::get, { TeamChosen(it, this) }) {
            ChooseTeam(parameter, this)
        }
    }
}

class CreateTeam(prev: TeamUpState) :
    MessageReceiverState("Choose a name for the new team", prev) {
    override fun isAllowed(command: Command) = true
    override fun getCommandNames(): List<String> = listOf()

    override fun successState(parameter: String): TeamUpState {
        return run {
            val starter = context.adressent?.username ?: "Nobody"
            val team = Team(parameter, starter)
            team.save()
            TeamChosen(team, this)
        }
    }
}

class TeamChosen(val team: Team, override val prev: TeamUpState) :
    PartyChosen<Team>(team, team.toJson(), prev) {
    override fun instance(party: Team): PartyChosen<*> {
        return TeamChosen(party, prev)
    }

    override fun isAllowed(command: Command): Boolean {
        return super.isAllowed(command) and when (command) {
            Command("hireLegio"), Command("fireLegio") -> context.adressent.isAdmin(team)
            else -> true
        }
    }

    override fun nextOrNull(command: Command, parameter: String?): TeamUpState? {
        return super.nextOrNull(command, parameter) ?: when (command) {
            Command("hirelegio") -> findObject(
                key = parameter,
                objectByKey = { Person.get(it) },
                stateByObject = { hireLegio(it.id) },
                stateByParameter = { hireLegio(it) }
            )
            Command("firelegio") -> findObject(
                key = parameter,
                objectByKey = { Person.get(it) },
                stateByObject = { fireLegio(it.id) },
                stateByParameter = { fireLegio(it) }
            )
            Command("newevent") -> CreateEvent(team, this)
            else -> null
        }
    }

    private fun hireLegio(personId: String?): TeamUpState {
        return personId?.let {
            return TeamChosen(team.also { it.legio hire personId }, prev)
        } ?: HireLegio(team, this)
    }

    private fun fireLegio(personId: String?): TeamUpState {
        return personId?.let {
            return TeamChosen(team.also { it.legio fire personId }, prev)
        } ?: FireLegio(team, this)
    }

    override fun getCommandNames(): List<String> {
        return super.getCommandNames() + listOf("hireLegio", "fireLegio", "newEvent")
    }
}

class HireLegio(override var party: Team, prev: TeamChosen) : Hire<Team>(party, prev) {
    override fun successState(parameter: String): TeamUpState? {
        return prev.instance(party.also { it.legio hire parameter })
    }
}

class FireLegio(override var party: Team, prev: TeamChosen) : Fire<Team>(party, prev) {

    override fun getCommandNames(): List<String> = listOf(*(party.legio.members) .toTypedArray())

    override fun successState(parameter: String): TeamUpState? {
        return prev.instance(party.also { it.legio fire parameter })
    }
}

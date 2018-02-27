package shtykh.teamup.controller.state

import shtykh.teamup.controller.Command
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.team.Team

class ChooseTeam(badName: String?, prev: TeamUpState) :
    MessageReceiverState("Give me team name better than \"$badName\"", prev) {
    override fun isAllowed(command: Command) = true

    override fun getCommandNames(): List<String> = listOf("newteam", *Team.directory.cache.keys.toTypedArray())

    override fun forCommand(command: Command, parameter: String?): TeamUpState? {
        return when (command) {
            Command("newTeam") -> CreateTeam(this)
            else -> null
        }
    }

    override fun successState(parameter: String): TeamUpState {
        return findObject(parameter, Team.Companion::get, this::teamChosen) {
            ChooseTeam(parameter, this)
        }
    }

    fun teamChosen(it: Team) = TeamChosen(it, this)
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

class TeamChosen(val team: Team, val prev: TeamUpState) :
    PartyChosen<Team>(team, "Team chosen: ${team.toJson()}", prev) {
    override fun instance(party: Team): PartyChosen<*> {
        return TeamChosen(party, prev)
    }

    override fun isAllowed(command: Command): Boolean {
        return super.isAllowed(command) and when (command) {
            Command("hirelegio"), Command("firelegio") -> context.adressent.isAdmin(team)
            else -> true
        }
    }

    override fun nextOrNull(command: Command, parameter: String?): TeamUpState? {
        return super.nextOrNull(command, parameter) ?: when (command) {
            Command("hireLegio") -> findObject(
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
            else -> null
        }
    }

    private fun hireLegio(personId: String?): TeamUpState {
        return personId?.let {
            return TeamChosen(team.also { it.legio hire personId }, prev)
        } ?: Start("Can't hire empty person as legio", context, chatId)
    }

    private fun fireLegio(personId: String?): TeamUpState {
        return personId?.let {
            return TeamChosen(team.also { it.legio fire personId }, prev)
        } ?: Start("Can't fire empty person as legio", context, chatId)
    }

    override fun getCommandNames(): List<String> {
        return super.getCommandNames() + listOf("hireLegio", "fireLegio")
    }
}

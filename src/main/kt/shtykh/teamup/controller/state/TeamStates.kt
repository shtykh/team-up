package shtykh.teamup.controller.state

import shtykh.teamup.controller.Command
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.team.Team

class ChooseTeam(badName: String?, prev: TeamUpState) :
    MessageReceiverState("Give me team name better than \"$badName\"", prev) {
    override fun isAllowed(command: Command) = true

    override fun getCommandNames(): List<String> = listOf("newteam")

    override fun forCommand(command: Command, parameter: String?): TeamUpState? {
        return when (command) {
            Command("newTeam") -> CreateTeam(this)
            else -> null
        }
    }

    override fun successState(parameter: String?): TeamUpState {
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

    override fun successState(parameter: String?): TeamUpState {
        return if (parameter == null) Start("can't create empty-named team", context, chatId) else {
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
            Command("hireLegio"), Command("fireLegio") -> context.adressent.isAdmin(team)
            else -> true
        }
    }

    override fun nextOrNull(command: Command, parameter: String?): TeamUpState? {
        return super.nextOrNull(command, parameter) ?: when (command) {
            Command("hireLegio") -> findObject(parameter, objectByKey = { Person.get(it) }, stateByObject = { hirelegio(it) })
            Command("firelegio") -> findObject(parameter, objectByKey = { Person.get(it) }, stateByObject = { firelegio(it) })
            else -> null
        }
    }

    fun hirelegio(person: Person): TeamUpState {
        team.legio hire person
        return TeamChosen(team, this)
    }

    fun firelegio(person: Person): TeamUpState {
        team.legio fire person
        return TeamChosen(team, this)
    }

    override fun getCommandNames(): List<String> {
        return listOf("hire", "fire", "hireLegio", "fireLegio", "setName", "help")
    }
}

package shtykh.teamup.controller.state

import com.github.ivan_osipov.clabo.api.model.User
import shtykh.teamup.controller.Command
import shtykh.teamup.domain.Manageble
import shtykh.teamup.domain.Party
import shtykh.teamup.domain.Person
import java.util.*

abstract class PartyChosen<T : Party<T>>(private val party: Party<T>, answer: String, prev: TeamUpState) :
    TeamUpState(answer, prev) {

    override fun isAllowed(command: Command): Boolean {
        return when (command) {
            Command("fire"), Command("hire"), Command("rename") -> context.adressent.isAdmin(party)
            else -> true
        }
    }

    override fun nextOrNull(command: Command, parameter: String?): TeamUpState? {
        return when (command) {
            Command("hire") -> findObject(parameter, objectByKey = { Person.get(it) }, stateByObject = { fire(it) })
            Command("fire") -> findObject(parameter, objectByKey = { Person.get(it) }, stateByObject = { hire(it) })
            Command("rename") -> Rename(party, this)
            else -> null
        }
    }

    private fun hire(person: Person): TeamUpState {
        return instance(party.instance() hire person)
    }

    private fun fire(person: Person): TeamUpState {
        return instance(party.instance() fire person)
    }

    fun setName(name: String): TeamUpState {
        party.name = name
        return instance(party.instance())
    }

    override fun getCommandNames(): List<String> {
        return Arrays.asList("hire, fire, setName, help")
    }

    abstract fun instance(party: T): PartyChosen<*>
}

fun User?.isAdmin(party: Party<*>) = (party is Manageble) && (party.admin == this?.username)

class Rename(private val party: Party<*>, prev: TeamUpState) :
    MessageReceiverState("Rename ${party.name}", prev) {

    override fun isAllowed(command: Command) = true

    override fun getCommandNames(): List<String> = emptyList()

    override fun successState(parameter: String?): TeamUpState {
        val oldName = party.name
        party.name = parameter ?: oldName
        return Start("$oldName was renamed to ${party.name}", context, chatId)
    }
}

package shtykh.teamup.controller.state

import com.github.ivan_osipov.clabo.api.model.User
import shtykh.teamup.controller.Command
import shtykh.teamup.domain.Manageble
import shtykh.teamup.domain.Party
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.event.Event
import shtykh.teamup.domain.team.Team
import shtykh.teamup.domain.team.util.FileSerializable
import java.util.*

abstract class PartyChosen<T : Party<T>> :
    TeamUpState {

    val party: Party<T>

    constructor(party: Party<T>, answer: String, prev: TeamUpState) : super(answer, prev) {
        this.party = party
        when(party) {
            is FileSerializable -> party.save()
        }
    }

    override fun isAllowed(command: Command): Boolean {
        return when (command) {
            Command("save"), Command("fire"), Command("hire"), Command("rename") -> context.adressent.isAdmin(party)
            else -> true
        }
    }

    override fun nextOrNull(command: Command, parameter: String?): TeamUpState? {
        return when (command) {
            Command("hire") -> findObject(parameter, objectByKey = { Person.get(it) }, stateByObject = { hire(it.id) }, stateByParameter = { hire(it)})
            Command("fire") -> findObject(parameter, objectByKey = { Person.get(it) }, stateByObject = { fire(it.id) }, stateByParameter = { fire(it)})
            Command("save") -> when(party) {
                is FileSerializable -> {
                    party.save()
                    instance(party as T)
                }
                else -> ErrorState("Can't save $party", this)
            }
            Command("rename") -> Rename(party, this)
            else -> null
        }
    }


    private fun hire(personId: String?): TeamUpState {
        return personId?.let {
            return instance(party.instance() hire it)
        } ?: Hire(party, this)
    }

    private fun fire(personId: String?): TeamUpState {
        return personId?.let {
            return instance(party.instance() fire it)
        } ?: Fire(party, this)
    }

    fun setName(name: String): TeamUpState {
        party.name = name
        return instance(party.instance())
    }

    override fun getCommandNames(): List<String> {
        return Arrays.asList("hire", "fire", "setName")
    }

    abstract fun instance(party: T): PartyChosen<*>
}

open class Hire<T: Party<T>>(open val party: Party<T>, override val prev: PartyChosen<T>) : MessageReceiverState("Give me person's id", prev) {
    override fun isAllowed(command: Command): Boolean = true

    override fun getCommandNames(): List<String> = listOf(*Person.directory.cache.keys.toTypedArray())

    override fun successState(parameter: String): TeamUpState? {
        return prev.instance((party hire parameter))
    }
}

open class Fire<T: Party<T>>(open val party: Party<T>, override val prev: PartyChosen<T>) : MessageReceiverState("Give me person's id", prev) {
    override fun isAllowed(command: Command): Boolean = true

    override fun getCommandNames(): List<String> = listOf(*(party.members).toTypedArray())

    override fun successState(parameter: String): TeamUpState? {
        return prev.instance(party fire parameter)
    }
}

fun User?.isAdmin(party: Party<*>) = (party is Manageble) && (party.admin == this?.username)

class Rename<out T: Party<T>>(private val party: Party<T>, prev: TeamUpState) :
    MessageReceiverState("Rename ${party.name}", prev) {

    override fun isAllowed(command: Command) = true

    override fun getCommandNames(): List<String> = emptyList()

    override fun successState(parameter: String): TeamUpState? {
        party.name = parameter
        return when(party) {
            is Team -> TeamChosen(party, this)
            is Event -> EventChosen(party, this)
            else -> null
        }
    }
}

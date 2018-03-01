package shtykh.teamup.controller.state

import shtykh.teamup.controller.Command
import shtykh.teamup.domain.Person

class ChoosePerson(badName: String?, prev: TeamUpState) :
    MessageReceiverState("Give me person name better than \"$badName\"", prev) {
    override fun isAllowed(command: Command) = true

    override fun getCommandNames(): List<String> = listOf("newPerson", *Person.directory.cache.keys.toTypedArray())


    override fun forCommand(command: Command, parameter: String?): TeamUpState? {
        return when (command) {
            Command("newPerson") -> CreatePerson(this)
            else -> null
        }
    }

    override fun successState(parameter: String): TeamUpState {
        return findObject(parameter, Person.Companion::get, this::personChosen) {
            ChoosePerson(parameter, this)
        }
    }

    fun personChosen(it: Person) = PersonChosen(it, this)
}

class CreatePerson(prev: TeamUpState) :
    MessageReceiverState("Choose a name for the new person", prev) {
    override fun isAllowed(command: Command) = true
    override fun getCommandNames(): List<String> = listOf()

    override fun successState(parameter: String): TeamUpState {
        return run {
            val starter = context.adressent?.username ?: "Nobody"
            val person = Person(parameter, starter)
            person.save()
            PersonChosen(person, this)
        }
    }
}

class PersonChosen(val person: Person, prev: TeamUpState) :
    TeamUpState(person.toJson(), prev) {

    override fun nextOrNull(command: Command, parameter: String?): TeamUpState? {
        return when (command) {
            Command("editJson") -> EditJson(person.also { it.save() }, this)
            Command("newPerson") -> CreatePerson(this)
            else -> error(command, person, parameter)
        }
    }

    override fun isAllowed(command: Command): Boolean {
        return true
    }

    override fun getCommandNames(): List<String> {
        return listOf("newPerson", "editJson")
    }
}

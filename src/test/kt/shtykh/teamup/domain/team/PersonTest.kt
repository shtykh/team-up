package shtykh.teamup.domain.team

import org.junit.Assert
import org.junit.Test
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.george
import shtykh.teamup.domain.saveBeatles

class PersonTest {

    @org.junit.After
    fun setUp() {
        saveBeatles()
    }

    @Test
    fun load() {
        val clone = Person.get("george")
        Assert.assertEquals(george, clone)
    }

    @Test
    fun fileName() {
        Assert.assertEquals("george", george.fileName())
    }

    @Test
    fun directory() {
        Assert.assertEquals("Person", george.directoryFile().name)
    }

    @Test
    fun getId() {
        Assert.assertEquals("george", george.id)
    }

    @Test
    fun name() {
        val name = "George Harrison"
        george.name = name
        Assert.assertEquals(name, george.name)
    }

}

package shtykh.teamup.domain.event

import org.junit.Assert
import org.junit.Test
import shtykh.teamup.domain.*
import java.io.InvalidObjectException
import java.util.*

class EventTest {

    val hamburg = Event("Going Hamburg", admin = paul.id)

    @Test
    fun load() {
        theBeatles()
        description()
        date()
        place()
        hire()
        hamburg.save()
        Assert.assertEquals(hamburg, Event.get("Going_Hamburg"))
    }

    @Test
    fun description() {
        hamburg.description = "Hamburg trip"
        Assert.assertEquals("Hamburg trip", hamburg.description)
    }

    @Test
    fun date() {
        val instance = Calendar.getInstance()
        instance.set(1960, 8, 17, 12, 0)
        hamburg.time = instance.time
        Assert.assertTrue(hamburg.time!!.before(Date()))
    }

    @Test
    fun place() {
        hamburg.place = "Indra Club"
        Assert.assertEquals("Indra Club", hamburg.place)
    }

    @Test
    fun hire() {
        val theBeatles = theBeatles()
        hamburg.teamId = theBeatles.id
        Assert.assertEquals(theBeatles.id, hamburg.teamId)
        hamburg.hireAll(john, paul, george, pete, stewart)
        Assert.assertEquals(5, hamburg.members.size)
        hamburg hire george hire george hire george hire george hire george // duplicated people are ignored
        Assert.assertEquals(5, hamburg.members.size)
    }

    @Test
    fun overhire() {
        hamburg.hireAll(john, paul, george, pete, stewart)
        Assert.assertEquals(5, hamburg.members.size)
        try {
            hamburg hire Person("freddie") hire Person("mic")
            Assert.assertFalse("Exception should have been thrown", true)
        } catch (ioe: InvalidObjectException) {
        }
        Assert.assertTrue("Over capacity", hamburg.members.size <= hamburg.capacity)
    }

    @Test
    fun overhire2() {
        hamburg.hireAll(john, paul, george, pete, stewart)
        Assert.assertEquals(5, hamburg.members.size)
        try {
            hamburg.hireAll(Person("freddie"), Person("mic"))
            Assert.assertFalse("Exception should have been thrown", true)
        } catch (ioe: InvalidObjectException) {
        }
        Assert.assertTrue("Over capacity", hamburg.members.size <= hamburg.capacity)
    }
}

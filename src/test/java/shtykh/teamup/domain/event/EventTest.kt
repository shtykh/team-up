package shtykh.teamup.domain.event

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import shtykh.teamup.domain.*
import java.io.InvalidObjectException
import java.util.*

@RunWith(Arquillian::class)
class EventTest {

    val hamburg = Event("Going Hamburg", admin = paul.id)

    @Test
    fun load() {
        hireBeatles()
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
        hamburg.teamName = beatles.name
        Assert.assertEquals(beatles.name, hamburg.teamName)
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
            hamburg.hireAll( Person("freddie"), Person("mic"))
            Assert.assertFalse("Exception should have been thrown", true)
        } catch (ioe: InvalidObjectException) {
        }
        Assert.assertTrue("Over capacity", hamburg.members.size <= hamburg.capacity)
    }

    companion object {

        @Deployment
        fun createDeployment(): JavaArchive {
            return ShrinkWrap.create(JavaArchive::class.java)
                    .addClass(Event::class.java)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
        }
    }

}

package shtykh.teamup.domain.team

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import shtykh.teamup.domain.Person
import shtykh.teamup.domain.george
import shtykh.teamup.domain.saveBeatles

@RunWith(Arquillian::class)
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

    companion object {

        @Deployment
        fun createDeployment(): JavaArchive {
            return ShrinkWrap.create(JavaArchive::class.java)
                    .addClass(Person::class.java)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
        }
    }

}

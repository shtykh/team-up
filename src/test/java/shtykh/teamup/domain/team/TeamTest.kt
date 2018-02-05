package shtykh.teamup.domain.team

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import shtykh.teamup.domain.*

@RunWith(Arquillian::class)
class TeamTest {


    @org.junit.Before
    fun setUp() {
        hireBeatles().save()
    }

    @Test
    fun load() {
        beatles.save()
        val clone = Team.get("The Beatles")
        Assert.assertEquals(beatles, clone)
        clone.legio hire john
        Assert.assertNotEquals(beatles, clone)
    }

    @org.junit.Test
    fun getDirectory() {
        Assert.assertEquals("Team", beatles.directoryFile().name)
    }

    @org.junit.Test
    fun getFileName() {
        Assert.assertEquals("The Beatles", beatles.fileName())
    }

    @org.junit.Test
    fun getMembers() {
        val members = beatles.members
        Assert.assertEquals(4, beatles.size())
        Assert.assertEquals(john.id, members[0])
    }

    @org.junit.Test
    fun getLegio() {
        Assert.assertEquals(2, beatles.legio.size())
    }

    @org.junit.Test
    fun hireMembers() {
        beatles hire paul
        beatles.hireAll(george, ringo)
        val members = beatles.members
        Assert.assertEquals(4, members.size)
        Assert.assertEquals(john.id, members[0])
        Assert.assertEquals(paul.id, members[1])
        beatles hire stewart
        Assert.assertEquals(5, members.size)


    }

    @org.junit.Test
    fun fireMembers() {
        beatles fire john fire george // :`(
        Assert.assertEquals(2, beatles.members.size)
    }

    @org.junit.Test
    fun hireLegio() {
        beatles.legio hire george
        Assert.assertEquals(george.id, beatles.legio.members[2])
    }

    @org.junit.Test
    fun fireLegio() {
        beatles.legio fire pete
        Assert.assertEquals(1, beatles.legio.size())
    }

    @org.junit.Test
    fun getName() {
        Assert.assertEquals("The Beatles", beatles.name)
    }

    @org.junit.Test
    fun setName() {
        beatles.name = "The Quarrymen"
        Assert.assertEquals("The Quarrymen", beatles.name)
        beatles.name = "The Beatles"
        Assert.assertEquals("The Beatles", beatles.name)
    }

    companion object {

        @Deployment
        fun createDeployment(): JavaArchive {
            return ShrinkWrap.create(JavaArchive::class.java)
                    .addClass(Team::class.java)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
        }
    }

}

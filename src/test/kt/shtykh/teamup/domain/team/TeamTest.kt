package shtykh.teamup.domain.team

import org.junit.Assert
import org.junit.Test
import shtykh.teamup.domain.*

class TeamTest {

    @Test
    fun load() {
        theBeatles()
        Team.directory.clearCache()
        val clone = Team.get("The_Beatles")
        Assert.assertEquals(theBeatles(), clone)
        clone!!.legio hire john
        Assert.assertFalse(theBeatles() equals clone)
    }

    @org.junit.Test
    fun getDirectory() {
        Assert.assertEquals("Team", theBeatles().directoryFile().name)
    }

    @org.junit.Test
    fun getFileName() {
        Assert.assertEquals("The_Beatles", theBeatles().fileName())
    }

    @org.junit.Test
    fun getMembers() {
        val members = theBeatles().members
        Assert.assertEquals(4, theBeatles().size())
        Assert.assertEquals(john.id, members[0])
    }

    @org.junit.Test
    fun getLegio() {
        Assert.assertEquals(2, theBeatles().legio.size())
    }

    @org.junit.Test
    fun hireMembers() {
        val theBeatles = theBeatles()
        theBeatles hire paul
        theBeatles.hireAll(george, ringo)
        val members = theBeatles.members
        Assert.assertEquals(4, members.size)
        Assert.assertEquals(john.id, members[0])
        Assert.assertEquals(paul.id, members[1])
        theBeatles hire stewart
        Assert.assertEquals(5, members.size)


    }

    @org.junit.Test
    fun fireMembers() {
        val theBeatles = theBeatles()
        theBeatles fire john fire george // :`(
        Assert.assertEquals(2, theBeatles.members.size)
    }

    @org.junit.Test
    fun hireLegio() {
        val theBeatles = theBeatles()
        theBeatles.legio hire george
        Assert.assertEquals(george.id, theBeatles.legio.members[2])
    }

    @org.junit.Test
    fun fireLegio() {
        val theBeatles = theBeatles()
        theBeatles.legio fire pete
        Assert.assertEquals(1, theBeatles.legio.size())
    }

    @org.junit.Test
    fun getName() {
        Assert.assertEquals("The Beatles", theBeatles().name)
    }

    @org.junit.Test
    fun setName() {
        val theBeatles = theBeatles()
        theBeatles.name = "The Quarrymen"
        Assert.assertEquals("The Quarrymen", theBeatles.name)
        theBeatles.name = "The Beatles"
        Assert.assertEquals("The Beatles", theBeatles.name)
    }

}

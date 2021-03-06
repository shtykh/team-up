package shtykh.teamup.domain.event

import shtykh.teamup.domain.Manageble
import shtykh.teamup.domain.Party
import shtykh.teamup.domain.team.util.Directory
import shtykh.teamup.domain.team.util.FileSerializable
import java.io.File
import java.io.InvalidObjectException
import java.util.*

class Event(override var name: String = "Event", var capacity: Int = 6, override var admin: String = "nobody", var teamId: String = "no one") : Party<Event>(name), FileSerializable, Manageble {

    var description: String? = null
    var time: Date? = null
    var place: String? = null

    override fun managers(): MutableList<String> = members

    override fun instance(): Event {
        return this
    }

    override fun directory(): EventDirectory {
        return directory
    }

    override fun fileName(): String {
        return id
    }

    override fun directoryFile(): File {
        return FileSerializable.dir(this.javaClass.simpleName)
    }

    override fun hire(newby: String): Event {
        if (members.size + 1 > capacity) {
            throw InvalidObjectException("$newby can not be hired, event $name is full")
        }
        return super.hire(newby)
    }

    override fun toString(): String {
        return "Event(name='$name', capacity=$capacity, description=$description, time=$time, place=$place, teamId=$teamId)"
    }


    companion object {
        val directory = EventDirectory()

        fun get(key: String): Event? {
            return directory.get(key) as Event?
        }
    }
}

class EventDirectory(override var dir: File = FileSerializable.dir("Event")) : Directory<FileSerializable>(dir) {
    override fun load(key: String): Event {
        return FileSerializable.load(FileSerializable.file(dir, key))
    }
}

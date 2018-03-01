package shtykh.teamup.domain.team

import shtykh.teamup.domain.Manageble
import shtykh.teamup.domain.Party
import shtykh.teamup.domain.PartyImpl
import shtykh.teamup.domain.team.util.Directory
import shtykh.teamup.domain.team.util.FileSerializable
import java.io.File

class Team(override var name: String = "Team Awesome", override var admin: String = "Nobody") : Party<Team>(name), FileSerializable, Manageble  {

    var legio: PartyImpl

    override fun managers(): MutableList<String> = members

    override fun instance(): Team {
        return this
    }

    override fun fileName(): String {
        return id
    }

    override fun directoryFile(): File {
        return FileSerializable.dir(this.javaClass.simpleName)
    }

    override infix fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as Team

        if (name != other.name) return false
        if (legio != other.legio) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + legio.hashCode()
        return result
    }
    override fun directory(): TeamDirectory {
        return directory
    }

    override fun toString(): String {
        return "Team(name='$name', admin='$admin', members='$members' legio=$legio)"
    }


    companion object {
        val directory = TeamDirectory()

        fun get(key: String): Team? {
            return directory.get(key) as Team?
        }
    }

    init {
        this hire admin
        this.legio = PartyImpl(name + " legio")
    }
}

class TeamDirectory(override var dir: File = FileSerializable.dir("Team")) : Directory<FileSerializable>(dir) {
    override fun load(key: String): Team {
        return FileSerializable.load(FileSerializable.file(dir, key))
    }
}


package shtykh.teamup.domain.team

import shtykh.teamup.domain.Party
import shtykh.teamup.domain.PartyImpl
import shtykh.teamup.domain.team.util.Directory
import shtykh.teamup.domain.team.util.FileSerializable
import java.io.File

class Team(override var name: String = "Team Awesome") : Party<Team>(name), FileSerializable  {
    override fun instance(): Team {
        return this
    }

    val legio: PartyImpl = PartyImpl(name + " legio")

    override fun fileName(): String {
        return id
    }

    override fun directoryFile(): File {
        return FileSerializable.dir(this.javaClass.simpleName)
    }

    override fun equals(other: Any?): Boolean {
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

    companion object {
        val directory = TeamDirectory()

        fun get(key: String): Team? {
            return directory.get(key) as Team?
        }
    }
}

class TeamDirectory : Directory<FileSerializable>() {
    override fun load(key: String): Team {
        return FileSerializable.load(FileSerializable.file(FileSerializable.dir("Team"), key))
    }
}


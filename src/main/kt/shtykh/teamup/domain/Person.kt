package shtykh.teamup.domain

import shtykh.teamup.domain.team.util.Directory
import shtykh.teamup.domain.team.util.FileSerializable
import shtykh.teamup.domain.team.util.FileSerializable.Companion.dir
import shtykh.teamup.domain.team.util.FileSerializable.Companion.file
import java.io.File

class Person(val id: String = "", var name: String = "John Doe") : FileSerializable {
    var tel: String? = null
    var email: String? = null

    override fun fileName(): String {
        return id
    }

    override fun directoryFile(): File {
        return dir("Person")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "Person(id='$id', name=$name)"
    }

    override fun directory(): PersonDirectory {
        return directory
    }

    companion object {
        val directory = PersonDirectory()

        fun get(key: String): Person? {
            return directory.get(key) as Person?
        }
    }
}

class PersonDirectory : Directory<FileSerializable>() {
    override fun load(key: String): Person {
        return FileSerializable.load(file(dir("Person"), key))
    }
}

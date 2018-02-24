package shtykh.teamup.domain.team.util

import shtykh.teamup.domain.util.Util
import java.io.File

interface FileSerializable: Jsonable {

    fun directoryFile(): File

    fun fileName(): String

    fun file(): File {
        return file(directoryFile(), fileName())
    }

    fun save() {
        directory().put(this)
    }

    fun directory(): Directory<FileSerializable>

    companion object {
        fun dir(name: String): File {
            val dir = File("""data/$name""")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

        inline fun <reified T : Jsonable> load(input: File): T {
            return Jsonable.fromJson(Util.read(input))
        }

        fun file(directoryFile: File, fileName: String): File {
            return File("""${directoryFile.absolutePath}/$fileName.json""");
        }
    }
}

abstract class Directory<T : FileSerializable> {
    val cache: MutableMap<String, T> = HashMap()

    abstract fun load(key: String): T

    fun save(value: T): File {
        val file = value.file()
        Util.write(file, value.toJson())
        return file
    }

    fun get(key: String): T? {
        return try {
            cache.computeIfAbsent(key, { load(key) })
        } catch (ex: Exception) {
            null
        }
    }

    fun put(value: T) {
        val key = value.fileName()
        if (cache[key] != value) {
            cache[key] = value
            save(value)
        }
    }

    fun clearCache() {
        cache.clear()
    }
}

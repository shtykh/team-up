package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.dsl.props

val commands = props(Command::class, "commands.properties")

class Command {
    constructor(_value: String = "") {
        this.value = _value
    }

    var value: String
        set(value) {
            field = value.toLowerCase()
        }

    override fun toString(): String {
        return "Command($value)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Command) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        fun forBotFather(): String {
            return reduce(this::forBotFatherMapper)
        }

        internal fun forHelp(): String {
            return reduce(this::forHelpMapper)
        }

        private fun reduce(mapper: (String, String) -> String) = commands
            .map { mapper(it.key.toString(), it.value.toString()) }
            .reduceRight { s, acc -> "$s\n$acc" }

        fun forBotFatherMapper(key: String, value: String) = "$key - $value"

        fun forHelpMapper(key: String, value: String) = "/$key : $value"

        fun get(key: String, mapper: (String, String) -> String): String {
            return mapper(key, commands.getProperty(key, ""))
        }
    }
}

package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.dsl.props

val commands = props(Command::class, "commands.properties")

class Command(private var _value: String = "") {
    var value: String
        set(value) {
            value.toLowerCase()
        }
        get() = _value

    companion object {
        fun forBotFather() : String {
            return reduce(this::forBotFatherMapper)
        }

        internal fun forHelp() : String {
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

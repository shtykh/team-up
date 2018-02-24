package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.dsl.props

val commands = props(Commands::class, "commands.properties")

object Commands {
    fun forBotFather() : String {
        return reduce(this::forBotFather)
    }

    fun forHelp() : String {
        return reduce(this::forHelp)
    }

    private fun reduce(mapper: (String, String) -> String) = commands.map { mapper(it.key.toString(), it.value.toString()) }.reduceRight { s, acc -> "$s\n$acc" }

    fun forBotFather(key: String, value: String) = "$key - $value"

    internal fun forHelp(key: String, value: String) = "/$key : $value"

    fun get(key: String, mapper: (String, String) -> String): String {
        return mapper.invoke(key, commands.getProperty(key, ""))
    }
}

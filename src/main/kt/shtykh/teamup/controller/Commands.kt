package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.dsl.props

val commands = props(Commands::class, "commands.properties")

object Commands {
    fun forStart() : String {
        return reduce(this::forStart)
    }

    fun forHelp() : String {
        return reduce(this::forHelp)
    }

    private fun reduce(mapper: (String, String) -> String) = commands.map { mapper.invoke(it.key.toString(), it.value.toString()) }.reduceRight { s, acc -> "$s\n$acc" }

    fun forStart(key: String, value: String) = "/$key : $value"

    private fun forHelp(key: String, value: String) = "/$key - $value"

    fun get(key: String, mapper: (String, String) -> String): String {
        return mapper.invoke(key, commands.getProperty(key, ""))
    }
}

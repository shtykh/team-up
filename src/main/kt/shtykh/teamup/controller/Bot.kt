package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.dsl.bot
import com.github.ivan_osipov.clabo.dsl.props

val botProperties = props(Bot::class, "bot.properties")

class Bot

fun main(args: Array<String>) {
    bot(botProperties) longPooling {
        configure {
            helloMessage("Hello! I'm Bot based on commands. Write '/'")
            updates {
                timeout = 30000
            }
        }

        commands {
            register("no") {
                it.update.message answer "No!"
            }
            register("yes") {
                it.update.message answer "Yes!"
            }
            registerForUnknown {
                it.update.message answer "Unknown command"
            }
        }

        onStart {
            println("Start with ${it.update.message?.from?.username}")
            it.message answer "Oh, hi ${it.message.from?.username}"
        }

        onHelp {
            println("Help with ${it.update.message?.from?.username}")
            it.update.message answer "I cannot help you"
        }

        onSettings {
            println("Settings with ${it.update.message?.from?.username}")
            it.update.message answer "I don't have settings"
        }
    }
}

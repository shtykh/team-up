package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.dsl.bot
import com.github.ivan_osipov.clabo.dsl.props
import shtykh.teamup.controller.Commands.forHelp
import shtykh.teamup.controller.Commands.forStart

val botProperties = props(Bot::class, "bot.properties")

class Bot

fun main(args: Array<String>) {
    var state: State = Start("Not Started")
    bot(botProperties) longPolling {
        configure {
            helloMessage("Hello! I'm Bot based on commands. Write '/'")
            updates {
                timeout = 3000
            }
        }

        commands {
            registerForUnknown {
                state = state.next(it.name, it.parameter)
                it.update.message answer state.answer()
            }
        }

        onStart {
            println("Start with ${it.update.message?.from?.username}")
            state = Start("Oh, hi ${it.message.from?.username}")
            it.message answer forStart()
        }

        onHelp {
            println("Help with ${it.update.message?.from?.username}")
            it.update.message answer forHelp()
        }

        onSettings {
            println("Settings with ${it.update.message?.from?.username}")
            it.update.message answer "I don't have settings"
        }
    }
}

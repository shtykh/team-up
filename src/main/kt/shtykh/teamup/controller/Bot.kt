package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.api.model.Message
import com.github.ivan_osipov.clabo.api.model.Update
import com.github.ivan_osipov.clabo.api.model.User
import com.github.ivan_osipov.clabo.dsl.bot
import com.github.ivan_osipov.clabo.dsl.props
import com.github.ivan_osipov.clabo.state.chat.ChatStateStore
import com.github.ivan_osipov.clabo.state.chat.StaticChatContext
import com.github.ivan_osipov.clabo.utils.ChatId
import shtykh.teamup.controller.state.Start
import shtykh.teamup.controller.state.TeamUpState

val botProperties = props(Main::class, "bot.properties")

class Main

fun main(args: Array<String>) {
    bot(botProperties) longPolling {
        val chatStore = TeamUpChatStore { message, s -> message answer s} // gross

        configure {
            updates {
                timeout = 300
            }
        }

        commands {
            registerForUnknown {
                val chatContext = chatStore.getChatContext(it.message.chat.id)
                println("${chatContext.state.javaClass.simpleName} with ${it.update.message?.from?.username}")
                chatContext
                        .answer(it.name)(it.message, it.update)
            }
        }

        chatting(chatStore){
        }

        onStart {
            if (it.message.from?.username == "shtykh") {
                println("Start with ${it.update.message?.from?.username}")
                chatStore.getChatContext(it.message.migrateToChatId ?: "default")
                val chatContext = chatStore.getChatContext(it.message.chat.id)
                it.update.message answer chatContext.start().answer()
            } else {
                it.update.message answer "It's out of service now, sorry."
            }
        }

        onHelp {
            println("Help with ${it.update.message?.from?.username}")
            it.update.message answer Command.forHelp()
        }

        onSettings {
            println("Settings with ${it.update.message?.from?.username}")
            it.update.message answer "I don't have settings"
        }
    }
}

class TeamUpChatStore(var answerFunction: (Message?, String) -> Unit) : ChatStateStore<TeamUpChatContext> {
    val contextMap: MutableMap<ChatId, TeamUpChatContext> = HashMap()

    override fun getChatContext(chatId: ChatId): TeamUpChatContext {
        val orDefault = contextMap.getOrDefault(chatId, TeamUpChatContext(chatId, answerFunction))
        contextMap.putIfAbsent(chatId, orDefault)
        return orDefault
    }

    override fun updateContext(chatId: ChatId, chatContext: TeamUpChatContext) {
        contextMap[chatId] = chatContext
    }
}

class TeamUpChatContext(var chatId: ChatId, var answerFunction: (Message?, String) -> Unit) : StaticChatContext() {

    var state: TeamUpState
    var adressent: User? = null

    init {
        state = start()
        messageCallbacks.add(this.answer())
    }

    fun answer(command: String = ""): (Message, Update) -> Unit = { message, update ->
        run {
            adressent = message.from
            val newState = state.next(command, message)
            state = newState
            answerFunction(update.message, state.answer())
        }
    }

    fun start(): TeamUpState {
        this.state = Start("Choose the domain to work with:", this, chatId)
        return this.state
    }
}


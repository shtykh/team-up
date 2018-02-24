package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.api.model.Message
import com.github.ivan_osipov.clabo.api.model.Update
import com.github.ivan_osipov.clabo.api.model.User
import com.github.ivan_osipov.clabo.dsl.bot
import com.github.ivan_osipov.clabo.dsl.props
import com.github.ivan_osipov.clabo.state.chat.ChatStateStore
import com.github.ivan_osipov.clabo.state.chat.StaticChatContext
import com.github.ivan_osipov.clabo.utils.ChatId
import shtykh.teamup.controller.Commands.forHelp

val botProperties = props(TeamUpState::class, "bot.properties")

fun main(args: Array<String>) {
    bot(botProperties) longPolling {
        val chatStore = TeamUpChatStore { message, s -> message answer s} // gross

        configure {
            helloMessage("Hello! I'm Bot based on commands. Write '/'")
            updates {
                timeout = 3000
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
            println("Start with ${it.update.message?.from?.username}")
            chatStore.getChatContext(it.message.migrateToChatId ?: "default")
            val chatContext = chatStore.getChatContext(it.message.chat.id)
            it.update.message answer chatContext.state.answer()
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

class TeamUpChatContext(chatId: ChatId, var answerFunction: (Message?, String) -> Unit) : StaticChatContext() {

    var state: TeamUpState
    var adressent: User? = null

    init {
        this.state = Start("start", this, chatId)
        messageCallbacks.add(this.answer())
    }

    fun answer(command: String? = null): (Message, Update) -> Unit = { message, update ->
        run {
            adressent = message.from
            val newState = state.next(command, message.text)
            state = newState
            answerFunction.invoke(update.message, state.answer())
        }
    }
}


package shtykh.teamup.controller

import com.github.ivan_osipov.clabo.api.model.Message
import com.github.ivan_osipov.clabo.api.model.Update
import com.github.ivan_osipov.clabo.dsl.bot
import com.github.ivan_osipov.clabo.dsl.props
import com.github.ivan_osipov.clabo.state.chat.ChatStateStore
import com.github.ivan_osipov.clabo.state.chat.StaticChatContext
import com.github.ivan_osipov.clabo.utils.ChatId
import shtykh.teamup.controller.Commands.forHelp
import shtykh.teamup.controller.Commands.forStart
import java.util.*

val botProperties = props(TeamUpState::class, "bot.properties")

fun main(args: Array<String>) {
    val chatStore = TeamUpChatStore()
    bot(botProperties) longPolling {
        configure {
            helloMessage("Hello! I'm Bot based on commands. Write '/'")
            updates {
                timeout = 3000
            }
        }

        commands {
            registerForUnknown {
                chatStore.getChatContext(it.message.chat.id)
                        .answer(it.name)(it.message, it.update)
            }
        }

        chatting(chatStore){
        }

        onStart {
            println("Start with ${it.update.message?.from?.username}")
            chatStore.getChatContext(it.message.migrateToChatId ?: "default")
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

class TeamUpChatStore : ChatStateStore<TeamUpChatContext> {
    val contextMap: MutableMap<ChatId, TeamUpChatContext> = HashMap()

    fun answer(context: TeamUpChatContext): (Message, Update) -> Unit = context.answer()

    override fun getChatContext(chatId: ChatId): TeamUpChatContext {
        return contextMap.getOrPut(chatId, { TeamUpChatContext(chatId).also { it.onMessage(answer(it)) } })
    }

    override fun updateContext(chatId: ChatId, chatContext: TeamUpChatContext) {
        contextMap[chatId] = chatContext
    }
}

private fun doAnswer(message: Message?, answer: String) {
    print(answer)
}

class TeamUpChatContext(chatId: ChatId) : StaticChatContext() {
    var state: TeamUpState = Start("start", this, chatId)
    fun answer(command: String? = null): (Message, Update) -> Unit = { message, update ->
        run {
            val newState = state.next(command, message.text)
            state = newState
            doAnswer(update.message, state.answer())
        }
    }
}


package lt.saltyjuice.dragas.chatty.v3.biscord
import kotlinx.coroutines.experimental.runBlocking
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.DeckController
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.HSCardController
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.StalkingController
import lt.saltyjuice.dragas.chatty.v3.discord.api.interceptor.RateLimitInterceptor
import lt.saltyjuice.dragas.chatty.v3.discord.controller.DiscordConnectionController
import lt.saltyjuice.dragas.chatty.v3.discord.main.DiscordClient
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.utility.khan4.Client
import lt.saltyjuice.dragas.utility.khan4.Khan
import okhttp3.logging.HttpLoggingInterceptor


fun main(args: Array<String>) = runBlocking<Unit>
{
    RateLimitInterceptor.shouldWait = true
    val client = Client().apply {
        okHttpBuilder.interceptors().removeIf { it is HttpLoggingInterceptor }
    }
    Khan.setClient(client)
    CardController.initialize()
    DiscordClient(
            DiscordConnectionController::class.java,
            DeckController::class.java,
            StalkingController::class.java,
            HSCardController::class.java).apply { work() }
}

inline fun Boolean.doIf(predicate: () -> Unit): Boolean
{
    if (this)
        predicate.invoke()
    return this
}

public fun Message.clearMyMentions()
{
    val id = DiscordConnectionController.getCurrentUserId()
    this.content = this.content.replace(Regex("<@!?$id>\\s*"), "")
    this.mentionedUsers.removeIf { it.id == id }
}


fun getenv(name: String, default: String): String
{
    return System.getenv(name) ?: default
}
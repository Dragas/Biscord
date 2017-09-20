package lt.saltyjuice.dragas.chatty.v3.biscord

import kotlinx.coroutines.experimental.runBlocking
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.BiscordUtility
import lt.saltyjuice.dragas.chatty.v3.discord.api.interceptor.RateLimitInterceptor
import lt.saltyjuice.dragas.chatty.v3.discord.controller.DiscordConnectionController
import lt.saltyjuice.dragas.chatty.v3.discord.main.DiscordClient
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) = runBlocking<Unit>
{
    //Utility.okHttpBuilder.addInterceptor(HeaderInterceptor(Pair("X-Requested-With", "XMLHttpRequest")))
    BiscordUtility.okHttpClientBuilder.connectTimeout(0, TimeUnit.MILLISECONDS)
    RateLimitInterceptor.shouldWait = true
    CardController.initialize()
    DiscordClient(
            CardController::class.java,
            DiscordConnectionController::class.java
            //DeckController::class.java,
            //StalkingController::class.java
    )
            .apply()
            {
                //initialize()
                work()
            }
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
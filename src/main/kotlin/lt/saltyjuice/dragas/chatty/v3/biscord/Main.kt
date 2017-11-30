package lt.saltyjuice.dragas.chatty.v3.biscord
import kotlinx.coroutines.experimental.runBlocking
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.DeckController
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.KommanderController
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.StalkingController
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.CardUtility
import lt.saltyjuice.dragas.chatty.v3.discord.controller.DiscordConnectionController
import lt.saltyjuice.dragas.chatty.v3.discord.main.DiscordClient
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.User
import lt.saltyjuice.dragas.utility.khan4.Client
import lt.saltyjuice.dragas.utility.khan4.Khan
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*


fun main(args: Array<String>) = runBlocking<Unit>
{
    val client = Client().apply {
        okHttpBuilder.interceptors().removeIf { it is HttpLoggingInterceptor }
    }
    Khan.setClient(client)
    CardUtility.initialize()
    DiscordClient(
            DiscordConnectionController::class.java,
            DeckController::class.java,
            StalkingController::class.java,
            KommanderController::class.java,
            CardController::class.java
    ).apply { work() }
}

public fun Boolean.doIf(predicate: () -> Unit): Boolean
{
    if (this)
        predicate.invoke()
    return this
}

public fun Boolean.doUnless(predicate: () -> Unit) : Boolean
{
    if(!this)
        predicate.invoke()
    return this
}

public fun Message.clearMyMentions()
{
    val id = DiscordConnectionController.getCurrentUserId()
    this.content = this.content.replace(Regex("<@!?$id>\\s*"), "")
    this.mentionedUsers.removeIf { it.id == id }
}

public operator fun Pair<Int, Int>.plus(another: Pair<Int, Int>): Pair<Int, Int>
{
    val first = this.first + another.first
    val second = this.second + another.second
    return Pair(first, second)
}

fun User.getAge(): Long
{
    return Date().time - (this.id.toLong().shr(22))
}

fun getenv(name: String, default: String): String
{
    return System.getenv(name) ?: default
}
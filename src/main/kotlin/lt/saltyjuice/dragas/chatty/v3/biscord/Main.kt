package lt.saltyjuice.dragas.chatty.v3.biscord

import kotlinx.coroutines.experimental.runBlocking
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.*
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.CardUtility
import lt.saltyjuice.dragas.chatty.v3.discord.controller.DiscordConnectionController
import lt.saltyjuice.dragas.chatty.v3.discord.main.DiscordClient
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.PrivateChannelBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Channel
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.User
import lt.saltyjuice.dragas.utility.khan4.Client
import lt.saltyjuice.dragas.utility.khan4.Khan
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*
import java.util.stream.Stream
import kotlin.streams.asStream


fun main(args: Array<String>) = runBlocking<Unit>
{
    val client = Client().apply {
        okHttpBuilder.interceptors().removeIf { it is HttpLoggingInterceptor }
    }
    Khan.setClient(client)
    CardUtility.initialize()
    DiscordClient(
            DiscordController::class.java,
            DeckController::class.java,
            StalkingController::class.java,
            KommanderController::class.java,
            CardController::class.java
    ).work()

    listOf<String>().joinToString()
}

public fun Boolean.doIf(predicate: () -> Unit): Boolean
{
    if (this)
        predicate.invoke()
    return this
}

public fun Boolean.doUnless(predicate: () -> Unit): Boolean
{
    if (!this)
        predicate.invoke()
    return this
}

public fun Message.clearMyMentions()
{
    val id = DiscordConnectionController.getCurrentUserId()
    this.content = this.content.replace(Regex("<@!?$id>\\s*"), "").trim()
    this.mentionedUsers.removeIf { it.id == id }
}


fun initiateChannel(author: User): Channel?
{
    try
    {
        val response = PrivateChannelBuilder(author.id).send()
        return response.body()
    }
    catch (err: Throwable)
    {
        err.printStackTrace()
    }
    return null
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

public fun <T, R> Stream<T>.flatterMap(mapper: ((T) -> Collection<R>)): Stream<R>
{
    return flatMap { mapper(it).stream() }
}

public fun <T, R> Stream<T>.flatterMapArray(mapper: (T) -> Array<R>): Stream<R>
{
    return flatterMap { mapper(it).toList() }
}

/**
 * Attempts to roughly separate the list into list of strings that are below target limit.
 */
@JvmOverloads
fun <T> Iterable<T>.joinToStrings(separator: String = ", ", limit: Int): List<String>
{
    val resultList = mutableListOf<MutableList<String>>()
    var currentList = mutableListOf<String>()
    var counter = 0
    val iterator = iterator()
    for (t in iterator)
    {
        if (t == null)
            continue
        val stringified = t.toString()
        if (counter + stringified.length + separator.length > limit)
        {
            resultList.add(currentList)
            currentList = mutableListOf()
            counter = 0
        }
        if (currentList.isNotEmpty())
            counter += separator.length
        counter += stringified.length
        currentList.add(stringified)
        if (!iterator.hasNext())
            resultList.add(currentList)
    }

    return resultList.map { it.joinToString(separator, limit = limit) }
}

inline fun <T> T.runIf(condition : Boolean, block : T.() -> T) : T
{
    return if(condition) block() else this
}
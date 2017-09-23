package lt.saltyjuice.dragas.chatty.v3.biscord.utility

import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.doIf
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Type
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import java.util.*
import kotlin.streams.toList

class CardWorker(private val request: Message)
{
    private var arguments = Array(1, { "" })
    private var shouldBeGold = false
    private var shouldBeImage = false
    private var shouldBeMany = false
    private var shouldIncludeCreated = false
    private var shouldFindById = false
    private var cards: Collection<Card> = mutableListOf()
    private var cardsObtained = false
    private var messages: List<MessageBuilder> = ArrayList<MessageBuilder>()

    init
    {
        parseArguments()
    }

    fun parseArguments()
    {
        parseArguments(request.content)
        if (arguments.isEmpty())
            return
        val exception = exceptionMap.keys.find { it.toLowerCase() == arguments[0].toLowerCase() }
        if (exception != null)
            arguments[0] = exceptionMap[exception]!!
        shouldBeGold = containsArgument(Param.GOLD)
        shouldBeImage = containsArgument(Param.IMAGE)
        shouldBeMany = containsArgument(Param.MANY)
        shouldIncludeCreated = containsArgument(Param.CREATES)
        shouldFindById = containsArgument(Param.ID)
    }

    private fun parseArguments(request: String)
    {
        this.arguments = request.split(Regex("\\s+-{1,2}")).toTypedArray()
    }

    private fun containsArgument(param: String): Boolean
    {
        return request.content.toLowerCase().startsWith(param).doIf()
        {
            request.content = request.content.replaceFirst(param, "")
            request.content = request.content.replaceFirst(" ", "")
        }
    }

    private fun containsArgument(param: Param): Boolean
    {
        return containsArgument(param.values)
    }

    private fun containsArgument(param: Array<out String>): Boolean
    {
        return param.find { value -> arguments.contains(value) } != null
    }

    @Throws(IllegalStateException::class)
    fun getCards(): CardWorker
    {
        checkValidity()
        val initialFilter = if (shouldBeMany) this::filterForMany else this::filterForSingle
        val initialList = if (shouldIncludeCreated) CardController.getCards() else CardController.getCollectable()
        var cards = getCards(initialList, initialFilter)
        if (shouldFindById)
        {
            try
            {
                val id = arguments[0].toInt()
                cards = getCards(CardController.getCards(), { it.dbfId == id })
            }
            catch (err: NumberFormatException)
            {

            }
        }
        else
        {
            if (cards.isEmpty() && !shouldBeMany)
            {
                cards = getCards(initialList, this::filterForMany)
            }
            if (cards.isEmpty() && !shouldIncludeCreated)
            {
                cards = getCards(CardController.getCards(), this::filterForMany)
            }
            if (cards.size == 1 && shouldIncludeCreated && !shouldFindById)
            {
                cards = cards.toTypedArray()[0].entourages
            }
        }
        this.cards = cards
        cardsObtained = true
        return this
    }

    fun buildMessage(): CardWorker
    {
        if (!cardsObtained) getCards()
        if (cards.isEmpty())
        {
            MessageBuilder(request.channelId)
                    .mention(request.author)
                    .append("no results were found for card name that's like `${arguments[0]}`")
                    .run { listOf(this) }
                    .apply { messages = this }
        }
        else
        {
            this.messages = cards.map(this::buildMessage)
        }
        return this
    }

    fun buildMessage(it: Card): MessageBuilder
    {
        if (!shouldBeImage)
        {
            return MessageBuilder(request.channelId)
                    .beginCodeSnippet("markdown")
                    .appendLine("[${it.name}][${it.cardId}][${it.dbfId}]")
                    .append("[${it.cost} Mana, ${it.rarity} ")
                    .apply { if (it.type == Type.MINION) this.append("${it.attack}/${it.health} ") }
                    .appendLine("${it.type?.name}]")
                    .appendLine("[Set: ${it.cardSet}]")
                    .appendLine(it.text)
                    .appendLine("> ${it.flavor}")
                    .endCodeSnippet()
                    .appendLine("https://hsreplay.net/cards/${it.dbfId}")
        }
        else
        {
            return MessageBuilder(request.channelId)
                    .append(it.img)
        }
    }

    fun send()
    {
        this.messages.forEach { it.send() }
    }

    fun isGold(): Boolean
    {
        return shouldBeGold
    }

    fun isValid(): Boolean
    {
        return arguments.isNotEmpty()
    }

    private fun checkValidity()
    {
        if (!isValid())
            throw IllegalStateException("Attempting to consume a not valid card worker")
    }

    private fun filterForSingle(it: Card): Boolean
    {
        return it.name.toLowerCase() == arguments[0].toLowerCase()
    }

    private fun getCards(listToFilter: Collection<Card>, filter: ((Card) -> Boolean)): Collection<Card>
    {
        return listToFilter
                .parallelStream()
                .filter(filter)
                .toList()
    }

    private fun filterForMany(it: Card): Boolean
    {
        return it.name.toLowerCase().replace(Regex("[^\\w\\s]"), "").contains(arguments[0])
    }

    private enum class Param(vararg val values: String)
    {
        CARD("card"),
        MANY("many", "m"),
        IMAGE("image", "i"),
        GOLD("gold", "g"),
        CREATES("creates", "c"),
        ID("id", "id");
    }

    companion object
    {
        @JvmStatic
        private val exceptionMap: HashMap<String, String> = hashMapOf(
                Pair("Pot of Greed", "Arcane Intellect"),
                Pair("Black Lotus", "Innervate"),
                Pair("Dr. Balance", "Dr. Boom"),
                Pair("Dr Balance", "Dr. Boom"),
                Pair("Shit", "\"(You)\""),
                Pair("nzoth", "N'Zoth, the Corruptor"),
                Pair("yogg", "Yogg-Saron, Hope's End")
        )
    }
}
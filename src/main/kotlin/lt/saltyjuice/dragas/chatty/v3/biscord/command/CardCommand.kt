package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Type
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import lt.saltyjuice.dragas.utility.kommander.main.Command
import java.util.*
import kotlin.streams.toList

@Name("hscard")
open class CardCommand : Command
{
    @Modifier("c", "-creates")
    private var shouldIncludeCreated: Boolean = false
    @Modifier("i", "-image")
    private var shouldBeImage: Boolean = false
    @Modifier("m", "-many")
    private var shouldBeMany: Boolean = false
    @Modifier("g", "-gold")
    private var shouldBeGold: Boolean = false
    @Modifier("id")
    private var shouldFindById: Boolean = false
    @Modifier("")
    private var cardName: String = ""
    @Modifier("chid")
    private var channelId: String = ""
    @Modifier("a", "-artwork")
    private var shouldShowArtwork: Boolean = false
    @Modifier("l", "-limit")
    private var limit: Int = 10

    private var list: Collection<Card> = mutableListOf()


    @Modifier("n")
    fun cardName(cardName: String)
    {
        this.cardName = exceptionMap.getOrDefault(cardName, cardName)
    }

    override fun execute()
    {
        if (!validate())
            return
        list = getCardList()
        if (list.isEmpty())
            onFailure()
        else
            onSuccess()
    }

    protected open fun onSuccess()
    {
        list
                .parallelStream()
                .limit(limit.toLong())
                .map(this::buildMessage)
                .forEach { if (channelId.isNotBlank()) it.send(channelId) }
    }

    protected open fun onFailure()
    {
        val sb = StringBuilder()
        sb.append("Unable to find any cards ")
        if (shouldFindById)
            sb.append("with id ")
        else
            sb.append("that are like ")
        sb.append(cardName)
        onError(sb.toString())
    }

    open fun validate(): Boolean
    {
        if (shouldBeGold)
        {
            onError("Due to changes in how cards are obtained, GOLDEN versions are unavailable. Just omit the -g/--gold modifier")
            return false
        }
        if (cardName.isBlank())
        {
            onError("Card names need to be prepended with `-n `")
            return false
        }
        return true
    }

    protected open fun onError(message: String)
    {
        if (channelId.isNotBlank()) MessageBuilder().append(message).send(channelId)
    }

    fun getCardList(): Collection<Card>
    {
        val initialFilter = if (shouldBeMany) this::filterForMany else this::filterForSingle
        val initialList = if (shouldIncludeCreated) CardController.getCards() else CardController.getCollectable()
        var cards = getCards(initialList, initialFilter)
        if (shouldFindById)
        {
            try
            {
                val id = cardName.toInt()
                cards = getCards(CardController.getCards(), { it.dbfId == id })
            }
            catch (err: NumberFormatException)
            {
                cards = listOf()
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
        return cards

    }

    private fun filterForSingle(it: Card): Boolean
    {
        return it.name.toLowerCase() == cardName.toLowerCase()
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
        return it.name.toLowerCase().replace(Regex("[^\\w\\s]"), "").contains(cardName)
    }


    private fun buildMessage(it: Card): MessageBuilder
    {
        return if (shouldBeImage) buildImage(it) else if (shouldShowArtwork) buildArtwork(it) else buildTextMessage(it)
    }

    private fun buildArtwork(it: Card): MessageBuilder
    {
        return MessageBuilder()
                .appendLine(it.artwork)
    }
    private fun buildTextMessage(it: Card): MessageBuilder
    {
        return MessageBuilder()
                .beginCodeSnippet("markdown")
                .appendLine("[${it.name}][${it.cardId}][${it.dbfId}]")
                .append("[${it.cost} Mana, ${it.playerClass} ${it.rarity} ")
                .apply()
                {
                    when (it.type)
                    {
                        Type.MINION -> append("${it.attack}/${it.health} ")
                        Type.WEAPON -> append("${it.attack}/${it.durability}")
                        Type.HERO -> append("${it.armor} Armor")
                        Type.SPELL ->
                        {
                        }
                    }
                }
                .appendLine("${it.type}]")
                .appendLine("[Set: ${it.cardSet}]")
                .appendLine(it.text)
                .endCodeSnippet()
                .appendLine(it.getStatisticsURL())
    }

    private fun buildImage(it: Card): MessageBuilder
    {
        val img = if (shouldBeGold) it.imgGold else it.img
        return MessageBuilder()
                .appendLine(img)
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
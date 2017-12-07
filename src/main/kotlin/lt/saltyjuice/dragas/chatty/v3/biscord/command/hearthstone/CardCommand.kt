package lt.saltyjuice.dragas.chatty.v3.biscord.command.hearthstone

import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Tag
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Type
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.CardUtility
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import java.util.*
import kotlin.streams.toList

@Name("hscard")
@Description("Returns any hearthstone card you would ever want")
open class CardCommand : ProtectedDiscordCommand()
{
    override val requiredPermissions: Long = 0
    @Modifier("c", "-creates")
    @JvmField
    @Description("Whether or not should the returned list consist of only created cards.")
    var shouldBeCreatedBy: Boolean = false

    @Modifier("i", "-image")
    @JvmField
    @Description("Whether or not should the returned list be of card images (prioritized over artwork)")
    var shouldBeImage: Boolean = false

    @Modifier("m", "-many")
    @JvmField
    @Description("Whether or not the result should be not exactly like the given card name")
    var shouldBeMany: Boolean = false

    @Modifier("id")
    @JvmField
    @Description("Whether or not given card name is actually an ID")
    var shouldFindById: Boolean = false

    private var cardName: String = ""

    @Modifier("a", "-artwork")
    @JvmField
    @Description("Whether or not the result should be an artwork.")
    var shouldShowArtwork: Boolean = false

    @Modifier("l", "-limit")
    @JvmField
    @Description("How many results at most should be returned.")
    var limit: Int = 10

    @Modifier("co", "-collectable")
    @JvmField
    @Description("Notes that there should only be collectable cards")
    var shouldBeCollectable = false

    var list: Collection<Card> = mutableListOf()


    @Modifier("")
    @Description("Card name or card ID to look for (required)")
    fun cardName(cardName: String)
    {
        HibernateUtil.executeSimpleTransaction()
        { session ->
            val query = session.createQuery("from Tag tag where lower(tag.key) = :cardname", Tag::class.java)
            query.setParameter("cardname", cardName)
            val result = query.resultList
            this.cardName = if(result.isNotEmpty()) result[0].value else cardName
        }
    }

    override fun execute()
    {
        respond("Let me get that...")
        searchForCards()
    }

    protected open fun searchForCards()
    {
        list = getCardList()
        if (list.isEmpty())
            onFailure()
        else
            onSuccess()
    }

    protected open fun onSuccess()
    {
        if (list.size > limit)
        {
            val text = StringBuilder()
            text.append("I have found ${list.size} cards. To list them all, use `hscard $cardName ")
            if (shouldFindById)
                text.append("-id ")
            if (shouldBeCreatedBy)
                text.append("-c ")
            if (shouldBeImage && !shouldShowArtwork)
                text.append("-i ")
            if (shouldShowArtwork)
                text.append("-a ")
            if (shouldBeMany)
                text.append("-m ")
            if (shouldBeCollectable)
                text.append("-co ")
            text.append("-l ${list.size}`")
            respond(text.toString())
        }
        silent = false
        list
                .parallelStream()
                .limit(limit.toLong())
                .map(this::buildMessage)
                .forEach(this::respondAsync)
    }

    open fun respondAsync(messageBuilder: MessageBuilder)
    {
        super.respondAsync(messageBuilder, null)
    }

    protected open fun onFailure()
    {
        val sb = StringBuilder().append("I am unable to find any cards ")
        sb.append("that are ")
        if (shouldBeCreatedBy)
            sb.append("created by ")
        if (shouldFindById)
            sb.append("id ")
        if (!shouldFindById && !shouldBeCreatedBy)
            sb.append("like ")
        sb
                .append("`")
                .append(cardName)
                .append("`")
        respond(sb.toString())
    }

    override fun onValidate(): Boolean
    {
        if (cardName.isBlank())
        {
            respond("Card name is required.")
            return false
        }
        return super.onValidate()
    }

    fun getCardList(): Collection<Card>
    {
        val initialFilter = if (shouldBeMany) this::filterForMany else this::filterForSingle
        val initialList = CardUtility.getCards()
        var cards = getCards(initialList, initialFilter)
        if (shouldFindById)
        {
            try
            {
                val id = cardName.toInt()
                cards = getCards(CardUtility.getCards(), { it.dbfId == id })
            }
            catch (err: NumberFormatException)
            {
                cards = listOf()
            }
        }
        else
        {
            if (cards.size == 1 && shouldBeCreatedBy && !shouldFindById)
            {
                cards = cards.toTypedArray()[0].entourages
            }
        }
        if (shouldBeCollectable && !shouldBeCreatedBy)
            cards = cards.parallelStream().filter(Card::collectible).toList()
        return cards

    }

    private fun filterForSingle(it: Card): Boolean
    {
        return it.name.toLowerCase().replace(wordsAndSpace, "") == cardName.replace(wordsAndSpace, "").toLowerCase()
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
        return it.name.toLowerCase().replace(wordsAndSpace, "").contains(cardName.replace(wordsAndSpace, "").toLowerCase())
    }


    private fun buildMessage(it: Card): MessageBuilder
    {
        return if (shouldBeImage) buildImage(it) else if (shouldShowArtwork) buildArtwork(it) else buildTextMessage(it)
    }

    private fun buildArtwork(it: Card): MessageBuilder
    {
        return MessageBuilder(chid)
                .appendLine(it.artwork)
    }

    private fun buildTextMessage(it: Card): MessageBuilder
    {
        return MessageBuilder(chid)
                .beginCodeSnippet("markdown")
                .appendLine("[${it.name}][${it.cardId}][${it.dbfId}]")
                .append("[${it.cost} Mana, ${it.playerClass} ${it.rarity} ")
                .apply()
                {
                    when (it.type)
                    {
                        Type.MINION -> append("${it.attack}/${it.health} ")
                        Type.WEAPON -> append("${it.attack}/${it.durability}")
                        Type.HERO -> if (it.armor > 0) append("${it.armor} Armor")
                        Type.SPELL ->
                        {
                        }
                    }
                }
                .apply { if(it.elite) append("ELITE") }
                .appendLine("${it.type}]")
                .appendLine("[Set: ${it.cardSet}]")
                .appendLine(it.text)
                .endCodeSnippet()
                .appendLine("<${it.getStatisticsURL()}>")
    }

    private fun buildImage(it: Card): MessageBuilder
    {
        val img = /*if (shouldBeGold) it.imgGold else*/ it.img
        return MessageBuilder(chid)
                .appendLine(img)
    }

    companion object
    {
        @JvmStatic
        private val wordsAndSpace = Regex("[^\\w\\s-]")
    }
}
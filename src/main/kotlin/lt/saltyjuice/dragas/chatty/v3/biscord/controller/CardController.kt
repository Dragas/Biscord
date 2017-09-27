package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.clearMyMentions
import lt.saltyjuice.dragas.chatty.v3.biscord.doIf
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.BiscordUtility
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.CardWorker
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.streams.toList

class CardController : Controller
{
    fun isCardRequest(request: Message): Boolean
    {
        return request.mentionsMe().doIf { request.clearMyMentions() } &&
                request
                        .content
                        .startsWith("card")
                        .doIf { request.content = request.content.replace("card ", "") }
    }

    @On(EventMessageCreate::class)
    @When("isCardRequest")
    fun onCardRequest(request: Message)
    {
        val cardWorker = CardWorker(request)
        if (cardWorker.isGold())
        {
            MessageBuilder()
                    .mention(request.author)
                    .append("Due to changes in how cards are obtained, GOLDEN versions are unavailable. Just omit the -g/--gold modifier")
                    .send(request.channelId)
            return
        }

        cardWorker
                .getCards()
                .buildMessage()
                .send()
    }

    companion object : Callback<Set<Card>>
    {
        @JvmStatic
        private var cardss = setOf<Card>()

        @JvmStatic
        @JvmOverloads
        fun initialize(cards: Set<Card> = setOf())
        {
            if (cards.isNotEmpty())
            {
                consumeCards(cards)
                return
            }
            if (cardss.isNotEmpty())
                return
            BiscordUtility.API.getCards().apply()
            {
                try
                {
                    val response = this.execute()
                    onResponse(this, response)
                }
                catch (err: Throwable)
                {
                    onFailure(this, err)
                }
            }
        }

        @JvmStatic
        private var collectableCards = setOf<Card>()

        @JvmStatic
        fun getCollectable(): Set<Card>
        {
            return collectableCards
        }

        @JvmStatic
        fun getCards(): Set<Card>
        {
            return cardss
        }

        @JvmStatic
        @Throws(NullPointerException::class)
        fun getCardById(dbfId: Int): Optional<Card>
        {
            return getCards()
                    .parallelStream()
                    .filter { it.dbfId == dbfId }
                    .findFirst()
        }

        @JvmStatic
        @Throws(NullPointerException::class)
        fun getCardById(dbfId: String): Optional<Card>
        {
            return getCards()
                    .parallelStream()
                    .filter { it.cardId == dbfId }
                    .findFirst()
        }

        @JvmStatic
        @Throws(NullPointerException::class)
        fun getCardsByStringId(list: List<String>): List<Card>
        {
            return getCards()
                    .parallelStream()
                    .filter { list.contains(it.cardId) }
                    .toList()
        }

        @JvmStatic
        fun getCardsByIntId(list: List<Int>): List<Card>
        {
            return getCards()
                    .parallelStream()
                    .filter { list.contains(it.dbfId) }
                    .toList()
        }

        @JvmStatic
        fun consumeCards(set: Set<Card>)
        {
            cardss = set
            collectableCards = cardss.parallelStream().filter(Card::collectible).toList().toSet()
            collectableCards
                    .parallelStream()
                    .filter { it.entourage.isNotEmpty() }
                    .forEach()
                    { entoraging ->
                        entoraging.entourages = entoraging
                                .entourage
                                .map(this@Companion::getCardById)
                                .filter { it.isPresent }
                                .map { it.get() }
                    }
        }

        @JvmStatic
        override fun onResponse(call: Call<Set<Card>>, response: Response<Set<Card>>)
        {
            if (response.isSuccessful)
                consumeCards(response.body()!!)
            else
                throw IllegalStateException("Failed to get cards from API")
        }

        @JvmStatic
        override fun onFailure(call: Call<Set<Card>>, up: Throwable)
        {
            throw up
        }
    }
}
package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.doIf
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.BiscordUtility
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import org.hibernate.query.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.streams.toList

object CardController : Callback<Set<Card>>
{

    @JvmStatic
    private var cardss = setOf<Card>()

    @JvmStatic
    @JvmOverloads
    fun initialize(cards: Set<Card> = setOf())
    {
        if (cardss.isNotEmpty() || initializeFromSet(cards) || initializeFromDatabase())
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

    fun initializeFromSet(cards: Set<Card>): Boolean
    {
        return cards.isNotEmpty().doIf { consumeCards(cards); }
    }

    fun initializeFromDatabase(): Boolean
    {
        val result = HibernateUtil.executeDetachedTransaction()
        { session ->
            val query: Query<Card> = session.createQuery("from Card", Card::class.java)
            val result = query.resultList
            result.toSet()
        }
        return result.isNotEmpty().doIf { consumeCards(result) }
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
        cardss
                .parallelStream()
                .filter { it.entourage.isNotEmpty() }
                .forEach()
                { entoraging ->
                    entoraging.entourages = entoraging
                            .entourage
                            .map(this::getCardById)
                            .filter(Optional<Card>::isPresent)
                            .map(Optional<Card>::get)
                }
    }

    @JvmStatic
    fun saveCards()
    {
        HibernateUtil.executeTransaction<Unit>({ session ->
            cardss
                    .parallelStream()
                    .forEach(session::persist)
            session.flush()
        })
    }


    override fun onResponse(call: Call<Set<Card>>, response: Response<Set<Card>>)
    {
        if (response.isSuccessful)
        {
            consumeCards(response.body()!!)
            saveCards()
        }
        else
            throw IllegalStateException("Failed to get cards from API")
    }


    override fun onFailure(call: Call<Set<Card>>, up: Throwable)
    {
        throw up
    }

}
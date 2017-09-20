package lt.saltyjuice.dragas.chatty.v3.biscord.entity

import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.buildSequence

open class Deck(val hash: String)
{
    private val byteArray = Base64.getDecoder().decode(hash)
    private var offset = AtomicInteger(0)
    private var version: Int = 0 // should be 1
    private var numberOfHeroes: Int = 0 // should be 1
    private var heroClass: PlayerClass = PlayerClass.Neutral // should be something else
    private var format: Format = Format.Invalid // 0 implies invalid
    private var valid = false
    private var deck: HashMap<Card, Int> = HashMap()
    private lateinit var producerJob: Sequence<Int>

    init
    {
        val initialByte = readInt()
        valid = initialByte != 0 || !readVersion() || !readFormat() || !readNumberOfHeroes() || !readHero()
        if (valid)
        {
            producerJob = launchProducer()
        }
    }

    private fun readInt(): Int
    {
        var length = 0
        var result = 0
        val bytes = byteArray.drop(offset.get())
        do
        {
            if (bytes.isEmpty())
                break
            val read = bytes[length].toInt()
            val value = read.and(0x7f)
            result = result.or(value.shl(length * 7))
            length++
        }
        while (read.and(0x80) == 0x80 && length < bytes.size)
        offset.getAndAdd(length)
        return result
    }

    /**
     * Returns false when version is invalid
     */
    private fun readVersion(): Boolean
    {
        version = readInt()
        return version == 1
    }

    private fun readNumberOfHeroes(): Boolean
    {
        numberOfHeroes = readInt()
        return numberOfHeroes == 1
    }

    private fun readFormat(): Boolean
    {
        format = Format.values().getOrElse(readInt(), { Format.Invalid })
        return format != Format.Invalid
    }

    private fun readHero(): Boolean
    {
        heroClass = PlayerClass.getById(readInt())
        return heroClass != PlayerClass.Neutral
    }

    private fun launchProducer(): Sequence<Int> = buildSequence()
    {
        while (offset.get() < byteArray.size)
        {
            val cardId = readInt()
            yield(cardId)
        }
    }

    private fun addCard(card: Card)
    {
        var count = deck[card] ?: 0
        count++
        deck[card] = count
    }

    open fun getAsDeck(): HashMap<Card, Int>
    {
        if (valid)
        {
            val cards = CardController.getCardsByIntId(producerJob.toList())
            cards.forEach(this::addCard)
        }
        return this.deck
    }

    enum class Format(val value: Int)
    {
        Invalid(0),
        Wild(1),
        Standard(2)
    }
}
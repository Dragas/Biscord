package lt.saltyjuice.dragas.chatty.v3.biscord.utility

import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ProducerJob
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.runBlocking
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.PlayerClass
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap

open class DeckWorker(private val hash: String)
{
    private var byteArray = ByteArray(0)
    private var offset = AtomicInteger(0)
    private var version: Int = 0 // should be 1
    private var numberOfHeroes: Int = 0 // should be 1
    private var heroClass: PlayerClass = PlayerClass.NEUTRAL // should be something else
    private var format: Format = Format.Invalid // 0 implies invalid
    private var valid = false
    private var deck: HashMap<Card, Int> = HashMap()
    private lateinit var producerJob: ProducerJob<Int>

    init
    {
        try
        {
            byteArray = Base64.getDecoder().decode(hash)
            val initialByte = readInt()
            valid = initialByte == 0 && readVersion() && readFormat() && readNumberOfHeroes() && readHero()
            if (valid)
            {
                producerJob = launchProducer()
            }
        }
        catch (err: IllegalArgumentException)
        {
            System.err.println("${err.message} caused by ${err.cause}")
        }
    }

    /**
     * Returns -1 on invalid byte array
     */
    private fun readInt(): Int
    {
        var length = 0
        var result = 0
        val bytes = byteArray.drop(offset.get())
        if (bytes.isEmpty())
            return -1
        do
        {
            val read = bytes[length].toInt()
            val value = read.and(0x7f)
            result = result.or(value.shl(length * 7))
            length++
        } while (read.and(0x80) == 0x80 && length < bytes.size)
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
        val playerClassInt = readInt()
        heroClass = PlayerClass.getById(playerClassInt)
        return true //heroClass != PlayerClass.Neutral
    }

    private fun launchProducer(): ProducerJob<Int> = produce<Int>(Unconfined)
    {
        while (offset.get() < byteArray.size)
        {
            val cardId = readInt()
            send(cardId)
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
            val list = ArrayList<Int>()
            addCards(list)
            addCards(list, 2)
            addCardMultiples(list)
            list
                    .map(CardController.Companion::getCardById)
                    .filter(Optional<Card>::isPresent)
                    .map(Optional<Card>::get)
                    .forEach(this::addCard)
        }
        return deck
    }

    private fun addCards(where: MutableCollection<Int>, multiplier: Int = 1)
    {
        val count = runBlocking { producerJob.receive() }
        repeat(count)
        {
            val id = runBlocking { producerJob.receive() }
            repeat(multiplier) { where.add(id) }
        }
    }

    private fun addCardMultiples(where: MutableCollection<Int>)
    {
        val count = runBlocking { producerJob.receive() }
        repeat(count)
        {
            val id = runBlocking { producerJob.receive() }
            val internalCount = runBlocking { producerJob.receive() }
            repeat(internalCount) { where.add(id) }
        }
    }

    open fun isValid(): Boolean
    {
        return valid
    }

    open fun getFormat(): Format
    {
        return this.format
    }

    open fun getClass(): PlayerClass
    {
        return this.heroClass
    }

    enum class Format(val value: Int)
    {
        Invalid(0),
        Wild(1),
        Standard(2)
    }
}
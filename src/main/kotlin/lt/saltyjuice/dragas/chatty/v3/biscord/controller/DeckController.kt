package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ProducerJob
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.runBlocking
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import java.util.*

open class DeckController : Controller
{
    fun decodeAsDeck() = runBlocking<Boolean>
    {
        val decoder: ProducerJob<Int>
        val deck = produce<Optional<Card>>(CommonPool)
        {
            repeat(2)
            { multiplier ->
                val count = decoder.receive()
                repeat(count)
                {
                    val id = decoder.receive()
                    repeat(multiplier + 1)
                    {
                        send(CardController.getCardById(id))
                    }
                }
            }
            val multiples = decoder.receiveOrNull()
            if (multiples != null)
            {
                repeat(multiples)
                {
                    val id = decoder.receive()
                    val count = decoder.receive()
                    repeat(count)
                    {
                        send(CardController.getCardById(id))
                    }
                }
            }
        }

        /*for (card in deck)
        {
            //val card = deck.receive()
            if (card.isPresent)
            {
                val card = card.get()
                var count = this@DeckController.deck.getOrDefault(card, 0)
                count++
                this@DeckController.deck[card] = count
            }
        }*/
        return@runBlocking true
    }

    fun decodeTest(request: Message): Boolean
    {
        val data = request.content.split(" ")
        return data.find(this::canDecode) != null
    }

    @On(EventMessageCreate::class)
    @When("decodeTest")
    fun onDecodeRequest(eventMessageCreate: Message)
    {
        //decoder = initializeDecoder()
        if (decodeAsDeck())
        {
            val messageBuilder = MessageBuilder(eventMessageCreate.channelId)
            messageBuilder.appendLine("# Class: ${this.heroClass.name}")
            messageBuilder.appendLine("# Format: ${this.format.name}")
            deck.toList()
                    .sortedBy({ it.first.name })
                    .sortedBy({ it.first.cost })
                    .forEach()
                    { (card, count) ->
                        messageBuilder.appendLine("# ${count}x (${card.cost}) ${card.name}")
                    }
            messageBuilder.send()
        }
    }
}
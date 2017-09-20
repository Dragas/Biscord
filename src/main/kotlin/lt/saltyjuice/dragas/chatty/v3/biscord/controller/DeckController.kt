package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller

open class DeckController : Controller
{
    /*fun decodeTest(request: Message): Boolean
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
    }*/
}

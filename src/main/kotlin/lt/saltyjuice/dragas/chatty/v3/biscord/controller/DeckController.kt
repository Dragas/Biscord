package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.utility.DeckWorker
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message

open class DeckController : Controller
{
    /*fun decodeTest(request: Message): Boolean
    {
        val data = request.content.split(" ")
        return data.find(this::canDecode) != null
    }*/

    @On(EventMessageCreate::class)
    fun onDecodeRequest(request: Message)
    {
        request
                .content
                .split(" ")
                .map(::DeckWorker)
                .filter(this::isValidWorker)
                .map(this::toMessageBuilder)
                .forEach { it.send(request.channelId) }
    }

    open fun isValidWorker(worker: DeckWorker): Boolean
    {
        return worker.isValid()
    }

    open fun toMessageBuilder(worker: DeckWorker): MessageBuilder
    {
        val messageBuilder = MessageBuilder()
        messageBuilder.appendLine("# Class: ${worker.getClass().name}")
        messageBuilder.appendLine("# Format: ${worker.getFormat().name}")
        messageBuilder.appendLine("")
        worker.getAsDeck().toList()
                .sortedBy { it.second }
                .sortedBy { it.first.name }
                .sortedBy { it.first.cost }
                .forEach { (card, count) ->
                    messageBuilder.appendLine("# ${count}x (${card.cost}) ${card.name}")
                }
        return messageBuilder
    }
}

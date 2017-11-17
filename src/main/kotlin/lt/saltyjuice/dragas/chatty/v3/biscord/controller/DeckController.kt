package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.utility.DeckWorker
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message

open class DeckController : Controller
{
    @On(EventMessageCreate::class)
    @When("notByMe")
    fun onDecodeRequest(request: Message)
    {
        request
                .content
                .split(Regex("\\s"))
                .parallelStream()
                .map { "deckode -c $it -chid ${request.channelId}" }
                .forEach(this::execute)
        /*.map(::DeckWorker)
        .filter(this::isValidWorker)
        .map(this::toMessageBuilder)
        .forEach { it.send(request.channelId) }*/
    }

    open fun execute(it: String)
    {
        KommanderController.execute(it)
    }

    open fun notByMe(message: Message): Boolean
    {
        return !message.author.isBot && !message.content.contains("#") && !message.mentionsMe()
    }

    open fun isValidWorker(worker: DeckWorker): Boolean
    {
        return worker.isValid()
    }

    open fun toMessageBuilder(worker: DeckWorker): MessageBuilder
    {
        val messageBuilder = MessageBuilder()
        worker.getClass().ifPresent { messageBuilder.appendLine("# Hero: ${it.name}").appendLine("# Class : ${it.playerClass}") }
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

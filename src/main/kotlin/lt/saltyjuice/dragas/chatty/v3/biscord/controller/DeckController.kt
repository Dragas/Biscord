package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
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
                .map { "deckode $it -chid ${request.channelId} -user ${request.author.id}" }
                .forEach(KommanderController.Companion::execute)
        /*.map(::DeckWorker)
        .filter(this::isValidWorker)
        .map(this::toMessageBuilder)
        .forEach { it.send(request.channelId) }*/
    }

    open fun notByMe(message: Message): Boolean
    {
        return !message.author.isBot && !message.content.contains("#") && !message.mentionsMe()
    }
}

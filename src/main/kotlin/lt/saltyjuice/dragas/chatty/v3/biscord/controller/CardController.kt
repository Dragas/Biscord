package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageUpdate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message

class CardController : Controller
{


    fun notByMe(request: Message): Boolean
    {
        return !request.author.isBot && request.content.contains(regex)
    }


    @On(EventMessageCreate::class)
    @When("notByMe")
    fun onMessage(request: Message)
    {
        regex.findAll(request.content)
                .map(MatchResult::value)
                .map { it.replace("[[", "").replace("]]", "").trim() }
                .filter(String::isNotBlank)
                .map { "hscard $it -chid ${request.channelId} -s -co" }
                .forEach(KommanderController.Companion::execute)
    }

    @On(EventMessageUpdate::class)
    @When("notByMe")
    fun onMessageEdit(request: Message)
    {
        onMessage(request)
    }

    companion object
    {
        @JvmStatic
        private val regex: Regex = Regex("\\[\\[[\\s\\w]+\\]\\]")
    }
}
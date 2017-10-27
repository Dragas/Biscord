package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.clearMyMentions
import lt.saltyjuice.dragas.chatty.v3.biscord.command.CardCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.doIf
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.utility.kommander.worker.Worker
import lt.saltyjuice.dragas.utility.kommander.worker.WorkerBuilder

class HSCardController : Controller
{
    init
    {
        println("Am I real?")
    }

    fun isCardRequest(request: Message): Boolean
    {
        return request.mentionsMe().doIf { request.clearMyMentions() } &&
                request
                        .content
                        .startsWith(name)
                        .doIf { request.content = request.content.replace("$name ", "") }
    }

    @When("isCardRequest")
    @On(EventMessageCreate::class)
    fun onMessage(request: Message)
    {
        if (!request.content.contains("-chid"))
            request.content = request.content + " -chid ${request.channelId}"
        worker.execute(request.content)
    }

    companion object
    {
        @JvmStatic
        private val worker: Worker

        @JvmStatic
        private val name: String

        init
        {
            val pair = WorkerBuilder(CardCommand::class.java).build()
            worker = pair.second
            name = pair.first
        }
    }
}
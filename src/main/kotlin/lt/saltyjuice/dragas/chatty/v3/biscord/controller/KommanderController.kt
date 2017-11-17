package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.clearMyMentions
import lt.saltyjuice.dragas.chatty.v3.biscord.command.CardCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.DeckCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.doIf
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.utility.kommander.main.Kommander

class KommanderController : Controller
{

    fun mentionsMe(message: Message): Boolean
    {
        return message.mentionsMe().doIf { message.clearMyMentions() }
    }

    @On(EventMessageCreate::class)
    @When("mentionsMe")
    fun onMessageCreate(message: Message)
    {
        if (message.content.toLowerCase().equals("help") || message.content.isBlank())
        {
            MessageBuilder().beginCodeSnippet("").append(kommander.description).endCodeSnippet().send(message.channelId)
            return
        }
        execute(message.content + " -chid ${message.channelId}")
    }

    companion object
    {
        @JvmStatic
        protected val kommander: Kommander = Kommander(CardCommand::class.java, DeckCommand::class.java).initialize()

        @JvmStatic
        fun execute(commandLine: String)
        {
            try
            {
                kommander.execute(commandLine)
            }
            catch (err: Exception)
            {
                err.printStackTrace()
            }
        }

    }
}
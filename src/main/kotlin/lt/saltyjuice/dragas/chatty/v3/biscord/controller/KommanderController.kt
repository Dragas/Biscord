package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.clearMyMentions
import lt.saltyjuice.dragas.chatty.v3.biscord.command.PurgeCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.RefreshCardDatabaseCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.StalkCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.TagCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.hearthstone.CardCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.hearthstone.DeckCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.hearthstone.ReminderCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.permission.DenyPermissionCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.command.permission.GrantPermissionCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.doIf
import lt.saltyjuice.dragas.chatty.v3.biscord.initiateChannel
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
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
            val channel = initiateChannel(message.author) ?: return;
            kommander
                    .description
                    .split(System.lineSeparator().repeat(2))
                    .filter(String::isNotBlank)
                    .map { MessageBuilder(channel.id).beginCodeSnippet("").append(it).endCodeSnippet() }
                    .forEach(MessageBuilder::sendAsync)
            return
        }
        execute(message.content + " -chid ${message.channelId} -user ${message.author.id}")
    }

    companion object
    {
        @JvmStatic
        protected val kommander: Kommander = Kommander(
                CardCommand::class.java,
                DeckCommand::class.java,
                PurgeCommand::class.java,
                GrantPermissionCommand::class.java,
                DenyPermissionCommand::class.java,
                RefreshCardDatabaseCommand::class.java,
                StalkCommand::class.java,
                TagCommand::class.java,
                ReminderCommand::class.java
        ).initialize()

        @JvmStatic
        fun execute(commandLine: String)
        {
            try
            {
                kommander.execute(commandLine)
            }
            catch (err: Exception)
            {
                Exception("While parsing $commandLine", err).printStackTrace()
            }
        }

    }
}
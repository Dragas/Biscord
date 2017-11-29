package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Embed
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.main.Command
import retrofit2.Callback

abstract class DiscordCommand : Command
{
    @Modifier("chid")
    @Description("Redundant. Jeeves overrides this parameter anyways.")
    protected var chid: String = ""


    @Modifier("user")
    @Description("Redundant. Jeeves overrides this value with caller's ID")
    protected var userId: String = ""


    @Modifier("s", "-silent")
    @JvmField
    @Description("Notes that there shouldn't be character related messages")
    protected var silent: Boolean = false


    protected open fun respond(message: String)
    {
        responseBuilder(chid, message).send()
    }

    protected open fun respond(embed: Embed)
    {
        responseBuilder(chid, embed).send()
    }

    @JvmOverloads
    protected open fun respondAsync(message: String, callback: Callback<Message>? = null)
    {
        respondAsync(responseBuilder(chid, message), callback)
    }

    @JvmOverloads
    protected open fun respondAsync(message: Embed, callback: Callback<Message>? = null)
    {
        respondAsync(responseBuilder(chid, message), callback)
    }

    @JvmOverloads
    protected open fun respondAsync(mb: MessageBuilder, callback: Callback<Message>? = null)
    {
        if (callback != null)
            mb.sendAsync(callback)
        else
            mb.sendAsync()
    }

    @JvmOverloads
    protected open fun responseBuilder(channelId: String, message: String = ""): MessageBuilder
    {
        return MessageBuilder(channelId).append(message)
    }

    protected open fun responseBuilder(channelId: String, embed: Embed): MessageBuilder
    {
        return MessageBuilder(channelId).embed(embed)
    }

    override fun validate(): Boolean
    {
        return chid.isNotBlank() && userId.isNotBlank()
    }
}
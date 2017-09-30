package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.clearMyMentions
import lt.saltyjuice.dragas.chatty.v3.biscord.doIf
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.core.route.When
import lt.saltyjuice.dragas.chatty.v3.discord.api.Utility
import lt.saltyjuice.dragas.chatty.v3.discord.controller.DiscordConnectionController
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventGuildMemberAdd
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageUpdate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.ChangedMember
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Channel
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.User
import lt.saltyjuice.dragas.chatty.v3.discord.message.response.ChannelBuilder
import java.text.SimpleDateFormat
import java.util.*


class StalkingController : Controller
{
    @On(EventMessageCreate::class)
    fun onMessage(request: Message)
    {
        checkForLinks(request)
    }

    @On(EventMessageUpdate::class)
    fun onMessageUpdate(request: Message)
    {
        checkForLinks(request)
    }

    @On(EventGuildMemberAdd::class)
    fun onNewGuildMember(event: ChangedMember)
    {
        extractData(event.user)
    }

    @On(EventMessageCreate::class)
    @When("containsID")
    fun onStalkRequest(request: Message)
    {
        val user = DiscordConnectionController.getUser(request.channelId, request.content) ?: return
        extractData(user.user)
    }

    fun containsID(request: Message): Boolean
    {
        return request.channelId == officeChannel
                && request
                .mentionsMe()
                .doIf { request.clearMyMentions() }
                && request.content.startsWith("stalk")
                .doIf { request.content = request.content.replace("stalk ", "") }
                .and(request.content.matches(Regex("^\\d+$")))

    }

    fun checkForLinks(message: Message)
    {
        if (message.author.isBot)
        {
            return
        }
        message.content = message.content
                .toLowerCase()
                .replace(Regex("[()]"), "")
                .replace(Regex("d[0o]t"), ".")
                .replace(Regex("sl[4a]sh"), "/")
                .replace(Regex("\\s\\."), ".")
                .replace(Regex("\\s/\\s"), "/")
        val author = DiscordConnectionController.getUser(message.channelId, message.author.id)
        val hasNoRole = author?.roles?.isEmpty() ?: true
        if (message.content.contains(linkRegex) && hasNoRole)
        {
            deleteMessage(message)
            notifyThatUser(message.author)
        }
    }

    fun notifyThatUser(user: User)
    {
        val channel = initiateChannel(user)
        if (channel == null)
        {
            MessageBuilder()
                    .appendLine("Failed to notify user ${user.username}#${user.discriminator}. They probably have me blocked!")
                    .send(officeChannel)
        }
        else
        {
            MessageBuilder()
                    .appendLine("I'm sorry, but you need to have a region to post links. Don't worry.")
                    .appendLine("The team were already notified of your presence.")
                    .send(channel.id)
        }
    }


    fun initiateChannel(author: User): Channel?
    {
        try
        {
            val response = Utility.discordAPI.createChannel(ChannelBuilder(author.id)).execute()
            return response.body()
        }
        catch (err: Throwable)
        {
            err.printStackTrace(System.err)
        }
        return null
    }

    fun deleteMessage(message: Message)
    {

        val dateAsString = sdf.format(message.timestamp)
        MessageBuilder()
                .appendLine("@everyone : ATTENTION! Possible spammer detected!")
                .appendLine("User (id): ${message.author.username}#${message.author.discriminator} (${message.author.id})")
                .appendLine("MessageID: ${message.id}")
                .append("Channel: ")
                .mentionId(message.channelId, MessageBuilder.MentionType.CHANNEL)
                .appendLine(" ")
                .appendLine("When: $dateAsString (timezones may apply)")
                .append("I was ")
                .apply { if (!deleteMessage(message.channelId, message.id)) append("not ") }
                .appendLine("able to delete the message.")
                .send(officeChannel)
    }

    fun deleteMessage(channelId: String, messageId: String): Boolean
    {
        try
        {
            val response = Utility.discordAPI.deleteMessage(channelId, messageId).execute()
            return response.isSuccessful
        }
        catch (err: Throwable)
        {
            err.printStackTrace(System.err)
        }
        return false
    }

    fun extractData(it: User)
    {
        val age = it.getAge()
        val ageVerbose = getVerboseAge(age)
        val date = Date()
        date.time -= age
        MessageBuilder()
                .appendLine("${it.username}#${it.discriminator}")
                .appendLine("Email: ${it.email}")
                .appendLine("Account age: $age ms (that's $ageVerbose)")
                .appendLine("Account creation date: ${sdf.format(date)}")
                .append("Account is ")
                .apply { if (!it.isVerified) append("not ") }
                .appendLine("verified.")
                .append("This user does ")
                .apply { if (!it.isTwoFactorAuthentificationEnabled) append("not ") }
                .appendLine("have two factor authentification enabled.")
                .send(officeChannel)
    }

    fun getVerboseAge(number: Long): String
    {
        val sb = StringBuilder()
        var age = number
        appendRemainder(sb, "day", age / day)
        age %= day
        appendRemainder(sb, "hour", age / hour)
        age %= hour
        appendRemainder(sb, "minute", age / minute)
        age %= minute
        appendRemainder(sb, "second", age / second, true)

        return sb.toString()
    }

    @JvmOverloads
    fun appendRemainder(sb: StringBuilder, modifier: String, remainder: Long, last: Boolean = false)
    {
        sb.append("$remainder $modifier")
        if (remainder != 1L)
            sb.append("s")
        if (!last)
            sb.append(", ")
    }

    companion object
    {
        @JvmStatic
        private val second = 1000L

        @JvmStatic
        private val minute = second * 60

        @JvmStatic
        private val hour = minute * 60

        @JvmStatic
        private val day = hour * 24

        @JvmStatic
        private val rawLinkRegex = System.getenv("RAW_LINK_REGEX")

        @JvmStatic
        private val linkRegex = Regex(rawLinkRegex)

        @JvmStatic
        private val officeChannel = System.getenv("office_id")

        @JvmStatic
        private val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm:ss z")

        @JvmStatic
        private val epochStart = 1420070400000L
    }

    private fun User.getAge(): Long
    {
        return Date().time - (this.id.toLong().shr(22) + epochStart)
    }
}


package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.DiscordConnectionController
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.StalkingController
import lt.saltyjuice.dragas.chatty.v3.biscord.getAge
import lt.saltyjuice.dragas.chatty.v3.biscord.getenv
import lt.saltyjuice.dragas.chatty.v3.discord.api.Utility
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Member
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.User
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.streams.toList

@Name("stalk")
@Description("returns information about particular users")
class StalkCommand : ProtectedDiscordCommand()
{
    override val requiredPermissions: Long = 8

    @Modifier("")
    protected fun parseUserIds(target: String)
    {
        ids = target
                .split(separationRegex)
                .parallelStream()
                .filter(String::isNotBlank)
                .filter { it.matches(numberRegex) }
                .distinct()
                .toList()
    }

    override fun onValidate(): Boolean
    {
        return chid == officeChannel && ids.isNotEmpty()
    }

    protected var ids: List<String> = listOf()

    override fun execute()
    {
        ids
                .parallelStream()
                .map { DiscordConnectionController.getUser(chid, it) }
                .filter { it != null }
                .map { it!! }
                .forEach(this::extractData)
    }

    protected open fun extractData(member: Member)
    {
        extractData(member.user)
    }

    protected fun extractData(it: User)
    {
        val age = it.getAge() - epochStart
        val ageVerbose = getVerboseAge(age)
        val date = Date()
        date.time -= age
        MessageBuilder(officeChannel)
                .appendLine("${it.username}#${it.discriminator}")
                .appendLine("Account age: $age ms (that's $ageVerbose)")
                .appendLine("Account creation date: ${sdf.format(date)}")
                .sendAsync()
    }

    protected fun getVerboseAge(number: Long): String
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
    protected fun appendRemainder(sb: StringBuilder, modifier: String, remainder: Long, last: Boolean = false)
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
        private val separationRegex = Regex("[<>@!]")

        @JvmStatic
        private val numberRegex = Regex("^\\d+$")

        @JvmStatic
        private val second = 1000L

        @JvmStatic
        private val minute = second * 60

        @JvmStatic
        private val hour = minute * 60

        @JvmStatic
        private val day = hour * 24

        @JvmStatic
        private val rawLinkRegex = getenv("RAW_LINK_REGEX", "")

        @JvmStatic
        private val officeChannel = getenv("office_id", "")

        @JvmStatic
        private val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm:ss z")

        @JvmStatic
        private val epochStart = 1420070400000L
    }
}
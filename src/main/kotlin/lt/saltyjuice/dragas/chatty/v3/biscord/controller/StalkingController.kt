package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.biscord.*
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.IgnorableThread
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.KnownThread
import lt.saltyjuice.dragas.chatty.v3.biscord.stalker.ThreadStalker
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.chatty.v3.core.controller.Controller
import lt.saltyjuice.dragas.chatty.v3.core.route.On
import lt.saltyjuice.dragas.chatty.v3.discord.api.Utility
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.PrivateChannelBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventGuildMemberAdd
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageUpdate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.ChangedMember
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Channel
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.User
import lt.saltyjuice.dragas.utility.khan4.Khan

import java.text.SimpleDateFormat
import lt.saltyjuice.dragas.utility.khan4.entity.Thread as KhanThread

class StalkingController : Controller
{
    val threadStalker : ThreadStalker = ThreadStalker()
    init
    {
        stalkThreads()
    }

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
        if(event.guildId == System.getenv("HSG"))
            KommanderController.execute("stalk ${event.user.id} -chid $officeChannel -user ${Settings.OWNER_ID}")
    }

    fun checkForLinks(message: Message)
    {
        if(DiscordController.getGuild(message.channelId).id != System.getenv("HSG"))
            return
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
            MessageBuilder(officeChannel)
                    .appendLine("Failed to notify user ${user.username}#${user.discriminator}. They probably have me blocked!")
                    .sendAsync()
        }
        else
        {
            MessageBuilder(channel.id)
                    .appendLine("I'm sorry, but you need to have a region to post links. Don't worry.")
                    .appendLine("The team were already notified of your presence.")
                    .sendAsync()
        }
    }


    fun deleteMessage(message: Message)
    {

        val dateAsString = sdf.format(message.timestamp)
        MessageBuilder(officeChannel)
                .appendLine("@everyone: ATTENTION! Possible spammer detected!")
                .appendLine("User (id): ${message.author.username}#${message.author.discriminator} (${message.author.id})")
                .appendLine("MessageID: ${message.id}")
                .append("Channel: ")
                .mentionId(message.channelId, MessageBuilder.MentionType.CHANNEL)
                .appendLine(" ")
                .appendLine("When: $dateAsString (timezones may apply)")
                .append("I was ")
                .apply { if (!deleteMessage(message.channelId, message.id)) append("not ") }
                .appendLine("able to delete the message.")
                .sendAsync()
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
            err.printStackTrace()
        }
        return false
    }

    fun stalkThreads()
    {
        threadStalker.addListener(this::stalkForExpiredThreads)
        threadStalker.addListener(this::stalkForNewThreads)
        threadStalker.begin()
    }

    fun stalkForExpiredThreads(actualThreads : List<KhanThread>)
    {
        val threadsItShouldIgnore = getIgnorableThreads(actualThreads)
        actualThreads
                .parallelStream()
                .filter { !threadsItShouldIgnore.contains(it.postNumber) }
                .filter { it.subject.contains("hsg", true) || it.comment.contains("playhearthstone", true) }
                .filter { it.replyCount >= postNotificationCount }
                .peek(StalkingController.Companion::saveExpiredThread)
                .map(this::expiredThreadToMessage)
                .forEach(MessageBuilder::sendAsync)
    }

    fun stalkForNewThreads(actualThreads : List<KhanThread>)
    {
        val ignorableThreads = getKnownThreads(actualThreads)
        actualThreads
                .parallelStream()
                .filter { it.subject.contains("hsg", true) || it.comment.contains("playhearthstone", true) }
                .filter { !ignorableThreads.contains(it.postNumber) }
                .peek(StalkingController.Companion::saveNewThread)
                .map(this::newThreadToMessage)
                .forEach(MessageBuilder::sendAsync)
    }

    fun getIgnorableThreads(actualThreads : List<KhanThread>) : List<Long>
    {
        val actualThreadIds = actualThreads.map(KhanThread::postNumber)
        return HibernateUtil.executeTransaction({ session ->
            session
                    .createQuery("delete from IgnorableThread where id not in :list")
                    .setParameterList("list", actualThreadIds)
                    .executeUpdate()
            session.createQuery("from IgnorableThread", IgnorableThread::class.java)
                    .resultList
                    .map(IgnorableThread::id)
        })
    }

    fun getKnownThreads(actualThreads: List<KhanThread>) : List<Long>
    {
        val actualThreadIds = actualThreads.map(KhanThread::postNumber)
        return HibernateUtil.executeTransaction({ session ->
            session
                    .createQuery("delete from KnownThread where id not in :list")
                    .setParameterList("list", actualThreadIds)
                    .executeUpdate()
            session.createQuery("from KnownThread", KnownThread::class.java)
                    .resultList
                    .map(KnownThread::id)
        })
    }

    private fun newThreadToMessage(thread : KhanThread) : MessageBuilder
    {
        return MessageBuilder("283769389071859732")
                .append("New ${thread.subject} thread found. You can view it at https://boards.4chan.org/vg/thread/${thread.postNumber}.")
    }

    private fun expiredThreadToMessage(thread: KhanThread): MessageBuilder
    {
        val mb = MessageBuilder(officeChannel)
                .append("Thread named ${thread.subject} (https://boards.4chan.org/vg/thread/${thread.postNumber}) is at ${thread.replyCount} post")
        if (thread.replyCount != 1)
            mb.appendLine("s")
        mb.appendLine("You should probably make a new thread.")
        return mb
    }

    companion object
    {

        @JvmStatic
        private val rawLinkRegex = getenv("RAW_LINK_REGEX", "")

        @JvmStatic
        private val linkRegex = Regex(rawLinkRegex)

        @JvmStatic
        private val officeChannel = getenv("office_id", "")

        @JvmStatic
        private val threadStalkRate = getenv("stalk_delay", "900000").toLong()

        @JvmStatic
        private val postNotificationCount = getenv("stalk_post_count", "720").toInt()

        @JvmStatic
        private val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm:ss z")

        @JvmStatic
        fun saveExpiredThread(thread: KhanThread)
        {
            HibernateUtil.executeSimpleTransaction({ session ->
                val khanThread = IgnorableThread(thread)
                //session.persist(khanThread)
                session.saveOrUpdate(khanThread)
            })
        }

        @JvmStatic
        fun saveNewThread(thread : KhanThread)
        {
            HibernateUtil.executeSimpleTransaction({ session ->
                val khanThread = KnownThread(thread)
                //session.persist(khanThread)
                session.saveOrUpdate(khanThread)
            })
        }
    }
}


package lt.saltyjuice.dragas.chatty.v3.biscord.controller


import lt.saltyjuice.dragas.chatty.v3.discord.message.general.*
/**
 * ODCC stands for Original discord connection controller.
 */
import lt.saltyjuice.dragas.chatty.v3.discord.controller.DiscordConnectionController as ODCC
import java.util.function.Predicate

/**
 * Discord controller that attempts to fix the visibility and synchronization issue
 * that original discord controller, provided by chatty framework, imposes.
 *
 * As a compatability measure, it still reflects on the original controller. Especially in
 * the [onReady] method.
 */
class DiscordController : DiscordConnectionController()
{
    override fun onGuildRoleCreate(request: RoleChanged)
    {
        synchronized(this)
        {
            super.onGuildRoleCreate(request)
        }
    }

    override fun onGuildRoleDelete(request: RoleDeleted)
    {
        synchronized(this)
        {
            super.onGuildRoleDelete(request)
        }
    }

    override fun onGuildCreate(request: CreatedGuild)
    {
        synchronized(this)
        {
            super.onGuildCreate(request)
        }
    }

    override fun onMemberAdd(request: ChangedMember)
    {
        synchronized(this)
        {
            super.onMemberAdd(request)
        }
    }

    override fun onMemberRemove(request: ChangedMember)
    {
        synchronized(this)
        {
            super.onMemberRemove(request)
        }
    }

    override fun onChannelCreate(request: Channel)
    {
        synchronized(this)
        {
            super.onChannelCreate(request)
        }
    }

    override fun onChannelDelete(request: Channel)
    {
        synchronized(this)
        {
            super.onChannelDelete(request)
        }
    }

    override fun onReady(request: Ready)
    {
        super.onReady(request)
        //a hack to maintain support with regular chatty library which sadly
        //depends on a PRIVATE FIELD IMPLEMENTATIONS
        //god i want to shoot 9 month ago me
        ODCC.javaClass.getMethod("access\$setReadyEvent\$p", ODCC.javaClass, Ready::class.java).apply {
            //this.isAccessible = true
            this.invoke(ODCC.Companion, ODCC.Companion, request)
        }
    }

    companion object
    {
        @JvmStatic
        fun getGuild(channelId: String): Guild
        {
            val channel = DiscordConnectionController.channels[channelId]!!
            val guild = guilds[channel.guildId]!!
            return guild
        }
    }
}
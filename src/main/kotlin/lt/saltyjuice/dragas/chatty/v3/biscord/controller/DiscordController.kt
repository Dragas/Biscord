package lt.saltyjuice.dragas.chatty.v3.biscord.controller


import lt.saltyjuice.dragas.chatty.v3.discord.message.general.*
import java.util.function.Predicate

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
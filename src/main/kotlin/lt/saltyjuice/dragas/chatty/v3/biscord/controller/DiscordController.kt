package lt.saltyjuice.dragas.chatty.v3.biscord.controller

import lt.saltyjuice.dragas.chatty.v3.discord.controller.DiscordConnectionController
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Guild
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.GuildIntegrationUpdate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.RoleChanged
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.RoleDeleted

class DiscordController : DiscordConnectionController()
{
    override fun onGuildIntegrationsUpdate(request: GuildIntegrationUpdate)
    {

    }

    override fun onGuildRoleCreate(request: RoleChanged)
    {

    }

    override fun onGuildRoleDelete(request: RoleDeleted)
    {

    }

    override fun onGuildRoleUpdate(request: RoleChanged)
    {

    }

    override fun onGuildUpdate(request: Guild)
    {

    }

    companion object
    {
        @JvmStatic
        fun getGuild(channelId : String) : Guild
        {
            val channel = channels[channelId]!!
            val guild = guilds[channel.guildId]!!
            return guild
        }
    }
}
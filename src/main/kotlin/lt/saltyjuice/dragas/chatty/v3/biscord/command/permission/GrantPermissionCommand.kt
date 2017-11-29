package lt.saltyjuice.dragas.chatty.v3.biscord.command.permission

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings
import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.User
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name

@Name("grant")
@Description("Used to grant particular permissions regarding \"\"\"vital\"\"\" commands")
class GrantPermissionCommand : PermissionCommand()
{
    override val requiredPermissions: Long = 2L
    override val verb: String = "grant"

    override fun execute()
    {
        val target = getTargetUser(targetUserId) ?: User(targetUserId)
        target.permissions = target.permissions.or(permission)
        HibernateUtil.executeSimpleTransaction({ session ->
            session.saveOrUpdate(target)
        })
    }
}
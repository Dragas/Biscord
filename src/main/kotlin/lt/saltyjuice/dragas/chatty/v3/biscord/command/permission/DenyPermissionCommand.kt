package lt.saltyjuice.dragas.chatty.v3.biscord.command.permission

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings
import lt.saltyjuice.dragas.chatty.v3.biscord.command.permission.PermissionCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.User
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier

class DenyPermissionCommand : PermissionCommand()
{
    override val requiredPermissions: Long = 2L
    override val verb: String = "deny"

    override fun execute()
    {
        val target = getTargetUser(targetUserId) ?: User(targetUserId)
        if (target.permissions.and(permission) == permission)
            target.permissions = target.permissions.xor(permission)
        HibernateUtil.executeSimpleTransaction({ session ->
            session.saveOrUpdate(target)
        })
    }
}
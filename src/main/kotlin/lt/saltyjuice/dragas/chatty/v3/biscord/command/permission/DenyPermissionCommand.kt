package lt.saltyjuice.dragas.chatty.v3.biscord.command.permission

import lt.saltyjuice.dragas.chatty.v3.biscord.entity.User
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Name

@Name("deny")
@Description("Used to deny permissions for \"\"\"vital\"\"\" commands")
class DenyPermissionCommand : PermissionCommand()
{
    override val requiredPermissions: Long = 2L
    override val verb: String = "deny"

    override fun applyChanges(user: User)
    {
        if (user.permissions.and(permission) == permission)
            user.permissions = user.permissions.xor(permission)
    }
}
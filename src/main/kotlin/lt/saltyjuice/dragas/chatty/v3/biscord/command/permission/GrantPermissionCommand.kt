package lt.saltyjuice.dragas.chatty.v3.biscord.command.permission

import lt.saltyjuice.dragas.chatty.v3.biscord.entity.User
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Name

@Name("grant")
@Description("Used to grant particular permissions regarding \"\"\"vital\"\"\" commands")
class GrantPermissionCommand : PermissionCommand()
{
    override val requiredPermissions: Long = 2L
    override val verb: String = "grant"

    override fun applyChanges(user: User)
    {
        user.permissions = user.permissions.or(permission)
    }
}
package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.User
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil

abstract class ProtectedDiscordCommand : DiscordCommand()
{
    protected abstract val requiredPermissions: Long

    final override fun validate(): Boolean
    {
        if (!super.validate())
            return false
        var permissionGranted = userId == Settings.OWNER_ID
        if (!permissionGranted)
        {
            var permissions = 0L
            permissionGranted = HibernateUtil.executeTransaction({ session ->
                if (userId.isNotBlank())
                {
                    val query = session.createQuery("from User where id = :userid", User::class.java)
                    query.setParameter("userid", userId)
                    val result = query.resultList
                    if (result.isNotEmpty())
                    {
                        val user = result[0]
                        permissions = user.permissions
                    }
                }
                return@executeTransaction permissions.and(requiredPermissions) == requiredPermissions
            })
        }
        return onValidate(permissionGranted)
    }

    open fun onValidate(permissionGranted: Boolean): Boolean
    {
        return permissionGranted
    }
}
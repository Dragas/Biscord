package lt.saltyjuice.dragas.chatty.v3.biscord.command.discord

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
            val user = getRequestingUser()
            val userPermissions = user?.permissions ?: 0
            permissionGranted = userPermissions.and(requiredPermissions) == requiredPermissions
        }
        if(!permissionGranted)
        {
            respond("Permission denied")
            return false
        }
        return onValidate()
    }

    open fun onValidate(): Boolean
    {
        return true
    }
    protected fun getRequestingUser() : User?
    {
        return getTargetUser(userId)
    }

    protected fun getTargetUser(userId : String) : User?
    {
        return HibernateUtil.executeTransaction({ session ->
            val query = session.createQuery("from User where id = :userid", User::class.java)
            query.setParameter("userid", userId)
            val result = query.resultList
            if (result.isNotEmpty())
            {
                result[0]
            }
            else
            {
                null
            }
        })
    }

}
package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.User
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil

abstract class ProtectedDiscordCommand : DiscordCommand()
{
    private var validateWasCalled = false
    protected abstract val requiredPermissions: Long

    final override fun validate(): Boolean
    {
        validateWasCalled = true
        if (!super.validate())
            return false
        var permissionGranted = requiredPermissions == 0L || userId == Settings.OWNER_ID
        if (!permissionGranted)
            permissionGranted = HibernateUtil.executeTransaction({ session ->
                if (userId.isNotBlank())
                {
                    val query = session.createQuery("from User where id = :userid", User::class.java)
                    query.setParameter("userid", userId)
                    val result = query.resultList
                    if (result.isNotEmpty())
                    {
                        val user = result[0]
                        return@executeTransaction user.permissions.and(requiredPermissions) == requiredPermissions
                    }
                }
                return@executeTransaction false
            })
        return onValidate(permissionGranted)
    }

    final override fun execute()
    {
        if (!validateWasCalled)
            throw KommanderException("super.validate() was not called")
        onExecute()
    }

    abstract fun onValidate(permissionGranted: Boolean): Boolean

    abstract fun onExecute()
}
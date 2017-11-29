package lt.saltyjuice.dragas.chatty.v3.biscord.command.discord

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings

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
        return onValidate(permissionGranted)
    }

    open fun onValidate(permissionGranted: Boolean): Boolean
    {
        if(!permissionGranted)
        {
            respondAsync("Access denied.")
            return false
        }
        return permissionGranted
    }


}
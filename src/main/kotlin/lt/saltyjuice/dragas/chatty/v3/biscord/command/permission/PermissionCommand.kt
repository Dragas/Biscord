package lt.saltyjuice.dragas.chatty.v3.biscord.command.permission

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings
import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier

abstract class PermissionCommand : ProtectedDiscordCommand()
{
    @Modifier("target")
    protected var targetUserId : String = ""

    @Modifier("")
    protected var permission : Long = 0L

    protected abstract val verb : String

    override fun onValidate(): Boolean
    {
        if(!super.onValidate())
            return false
        if(permission == 0L)
        {
            respondAsync("You don't need to $verb default permissions \uD83E\uDD14")
            return false
        }
        targetUserId = targetUserId.replace(Regex("[<>@!]"), "")
        if(targetUserId.isBlank())
        {
            respondAsync("Target user modifier is required")
            return false
        }
        if(Settings.OWNER_ID == userId)
        {
            return true
        }
        val currentUsersPermissions = getRequestingUser()?.permissions ?: 0
        if(currentUsersPermissions.and(permission) != permission)
        {
            respondAsync("Current user can't $verb such permissions")
            return false
        }
        return true
    }
}
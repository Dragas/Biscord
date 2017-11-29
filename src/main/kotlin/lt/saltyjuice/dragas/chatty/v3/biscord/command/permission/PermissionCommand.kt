package lt.saltyjuice.dragas.chatty.v3.biscord.command.permission

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings
import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier

abstract class PermissionCommand : ProtectedDiscordCommand()
{
    @Modifier("target")
    protected var targetUserId : String = ""

    @Modifier("")
    protected var permission : Long = 0L

    protected abstract val verb : String

    override fun onValidate(permissionGranted: Boolean): Boolean
    {
        if(permission == 0L)
        {
            MessageBuilder(chid).append("You don't need to $verb default permissions \uD83E\uDD14").sendAsync()
            return false
        }
        targetUserId = targetUserId.replace(Regex("[<>@!]"), "")
        if(targetUserId.isBlank())
        {
            MessageBuilder(chid).append("Target user modifier is required").sendAsync()
            return false
        }
        val currentUsersPermissions = getRequestingUser()?.permissions ?: 0
        if(currentUsersPermissions.and(permission) != permission || Settings.OWNER_ID != userId)
        {
            MessageBuilder(chid).append("Current user can't $verb such permissions").sendAsync()
        }
        return true
    }
}
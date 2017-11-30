package lt.saltyjuice.dragas.chatty.v3.biscord.command.permission

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings
import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.User
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import kotlin.streams.toList

abstract class PermissionCommand : ProtectedDiscordCommand()
{
    @Modifier("target")
    protected fun targetUserId(id: String)
    {
        targetIds = id
                .split(separationRegex)
                .parallelStream()
                .filter(String::isNotBlank)
                .filter { it.matches(numberRegex) }
                .distinct()
                .toList()

    }

    protected var targetIds: List<String> = listOf()

    @Modifier("")
    protected var permission: Long = 0L

    protected abstract val verb: String

    override fun onValidate(): Boolean
    {
        if (!super.onValidate())
            return false
        if (permission == 0L)
        {
            respondAsync("You don't need to $verb default permissions \uD83E\uDD14")
            return false
        }
        if (targetIds.isEmpty())
        {
            respondAsync("Target user modifier is required")
            return false
        }
        if (Settings.OWNER_ID == userId)
        {
            return true
        }
        val currentUsersPermissions = getRequestingUser()?.permissions ?: 0
        if (currentUsersPermissions.and(permission) != permission)
        {
            respondAsync("Current user can't $verb such permissions")
            return false
        }
        return true
    }

    final override fun execute()
    {
        targetIds
                .parallelStream()
                .map(this::getTargetUser)
                .peek(this::applyChanges)
                .peek(this::saveChanges)
                .forEach(this::respondAboutUser)
    }

    open fun saveChanges(user : User)
    {
        HibernateUtil.executeSimpleTransaction({ session ->
            session.saveOrUpdate(user)
        })
    }

    open fun respondAboutUser(user : User)
    {
        respond("Done. Now <@${user.id}> has permission level of ${user.permissions}")
    }

    abstract fun applyChanges(user : User)

    companion object
    {
        @JvmStatic
        private val separationRegex = Regex("[<>@!]")

        @JvmStatic
        private val numberRegex = Regex("^\\d+$")
    }
}
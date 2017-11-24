package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.Settings
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.User
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import lt.saltyjuice.dragas.utility.kommander.main.Command
import kotlin.system.measureTimeMillis

@Name("purge")
@Description("Purges internal data when necessary.")
class PurgeCommand : DiscordCommand()
{
    @Modifier("t", "table")
    var table : String = ""

    override fun execute()
    {
        MessageBuilder(chid).append("Purging table $table...").send()
        try
        {
            val result = measureTimeMillis {
                HibernateUtil.executeTransaction({ session ->
                    val query = session.createQuery("select from $table")
                    query.resultList.forEach(session::delete)
                })
            }
            MessageBuilder(chid).append("Purge complete. Request took $result ms. If this was some sensitive data I should get restarted.").send()
        }
        catch (err : Exception)
        {
            err.printStackTrace()
            MessageBuilder(chid).append("Purging failed. Reason '$err'. Check logs for more information").send()
        }
    }

    override fun validate(): Boolean
    {
        if(chid.isBlank())
            return false
        if(userId.isBlank())
        {
            MessageBuilder(chid).append("User ID is required").sendAsync()
            return false
        }
        return userId == Settings.OWNER_ID || HibernateUtil.executeTransaction({ session ->
            val query = session.createQuery("from User where id = :userid", User::class.java)
            query.setParameter("userid", userId)
            val result = query.resultList
            if(result.isNotEmpty())
            {
                val user = result[0]
                return@executeTransaction user.permissions.and(PERMISSION_ID) == PERMISSION_ID
            }
            return@executeTransaction false
        })
    }

    companion object
    {
        @JvmStatic
        val PERMISSION_ID = 1L
    }
}
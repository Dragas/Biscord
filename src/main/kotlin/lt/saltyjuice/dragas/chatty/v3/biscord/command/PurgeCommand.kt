package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import kotlin.system.measureTimeMillis

@Name("purge")
@Description("Purges internal data when necessary.")
class PurgeCommand : ProtectedDiscordCommand()
{
    @Modifier("t", "table")
    var table: String = ""

    override val requiredPermissions: Long = 1L

    override fun execute()
    {
        //super.execute()
        purge()
    }

    protected open fun purge()
    {
        MessageBuilder(chid).append("Purging table $table...").send()
        val mb = MessageBuilder(chid)
        try
        {
            var rowsAffected = 0
            val result = measureTimeMillis {
                HibernateUtil.executeTransaction({ session ->
                    val query = session.createQuery("delete from $table")
                    rowsAffected = query.executeUpdate()
                })
            }
            mb.append("Purge complete. Request took $result ms and affected $rowsAffected rows. If this was some sensitive data I should get restarted.")
        }
        catch (err: Exception)
        {
            err.printStackTrace()
            mb.append("Purging failed. Reason `$err`. Check logs for more information")
        }
        mb.send()
    }

    override fun onValidate(permissionGranted: Boolean): Boolean
    {
        return permissionGranted
    }
}
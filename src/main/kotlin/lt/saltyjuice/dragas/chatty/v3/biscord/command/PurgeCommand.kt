package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.CardUtility
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.chatty.v3.discord.message.builder.MessageBuilder
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import kotlin.system.measureTimeMillis

@Name("purge")
@Description("Purges internal data when necessary.")
open class PurgeCommand : ProtectedDiscordCommand()
{
    @Modifier("t", "table")
    open var table: String = ""

    override val requiredPermissions: Long = 1L

    override fun execute()
    {
        //super.execute()
        purge()
    }

    protected open fun purge()
    {
        respond("Purging table $table...")
        try
        {
            var rowsAffected = 0
            val result = measureTimeMillis()
            {
                HibernateUtil.executeTransaction({ session ->
                    val query = session.createQuery("delete from $table")
                    rowsAffected = query.executeUpdate()
                })
            }
            respond("Purge complete. Request took $result ms and affected $rowsAffected rows. If this was some sensitive data I should get restarted.")
        }
        catch (err: Exception)
        {
            err.printStackTrace()
            respond("Purging failed. Reason `$err`. Check logs for more information")
        }
    }
}
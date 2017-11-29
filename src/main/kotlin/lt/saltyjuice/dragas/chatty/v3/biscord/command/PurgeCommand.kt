package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil

@Name("purge")
@Description("Purges internal data when necessary.")
class PurgeCommand : ProtectedDiscordCommand()
{
    @Modifier("t", "table")
    var table : String = ""

    override val requiredPermissions: Long = 1L

    override fun onExecute()
    {
        //super.execute()
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

    override fun onValidate(permissionGranted: Boolean): Boolean
    {
        if(chid.isBlank())
            return false
        if(userId.isBlank())
        {
            MessageBuilder(chid).append("User ID is required").sendAsync()
            return false
        }
        return permissionGranted
    }
}
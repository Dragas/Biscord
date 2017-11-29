package lt.saltyjuice.dragas.chatty.v3.biscord

object Settings
{
    @JvmStatic
    val OWNER_ID_ENV = "OWNER_ID"

    @JvmStatic
    val OWNER_ID = System.getenv(OWNER_ID_ENV) ?: throw NullPointerException("Owner ID is required")
}
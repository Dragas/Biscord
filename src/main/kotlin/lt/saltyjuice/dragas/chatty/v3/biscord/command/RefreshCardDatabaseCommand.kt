package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.CardUtility
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

@Name("refresh-cards")
@Description("Refreshes the card database.")
class RefreshCardDatabaseCommand : PurgeCommand()
{
    override val requiredPermissions: Long = 1L

    override var table : String = "Card"

    override fun execute()
    {
        super.execute()
        try
        {
            respond("Refreshing card database...")
            val time = measureTimeMillis()
            {
                CardUtility.consumeCards(setOf())
                CardUtility.initialize()
            }
            respondAsync("Done. Execution took $time ms")
        }
        catch (err: Exception)
        {
            respondAsync("Something happened: `err`. Check logs for more information")
            throw err
        }
    }
}
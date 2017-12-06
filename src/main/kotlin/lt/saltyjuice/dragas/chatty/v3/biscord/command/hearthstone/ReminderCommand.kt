package lt.saltyjuice.dragas.chatty.v3.biscord.command.hearthstone

import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Type
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.CardUtility
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import lt.saltyjuice.dragas.utility.kommander.main.Command
import org.hibernate.criterion.Subqueries
import org.hibernate.query.Query
import javax.persistence.criteria.Subquery
import kotlin.streams.toList

@Name("remind-me")
class ReminderCommand : ProtectedDiscordCommand()
{
    override val requiredPermissions: Long = 0

    @Modifier("")
    @Description("args which are parsed")
    private var defaultArg: String = ""


    override fun onValidate(): Boolean
    {
        if (defaultArg.isBlank())
        {
            respondAsync("Expected some leads. Got nothing.")
            return false
        }
        return true
    }

    override fun execute()
    {
        val mechanics = CardUtility
                .mechanics
                .stream()
                .filter { defaultArg.contains(it, true) }
                .peek { defaultArg.replace(it, "", true) }
                .map { it.replace(" ", "_", true) }
                .toList()
        var shouldFilterByStats = false
        var attack = 0
        var health = 0
        var type = Type.MINION
        types.firstOrNull { defaultArg.contains(it, true) }?.apply { defaultArg.replace(this, "", true); type = Type.valueOf(this) }
        if (attackOrHealthRegex.containsMatchIn(defaultArg))
        {
            val groups = attackOrHealthRegex.find(defaultArg)!!.groupValues
            attack = groups[1].toInt()
            health = groups[2].toInt()
            shouldFilterByStats = true
            defaultArg = defaultArg.replace(attackOrHealthRegex, "")
        }
        HibernateUtil.executeSimpleTransaction()
        { session ->
            val cb = session.criteriaBuilder
            val query = cb.createQuery(Card::class.java)
            val root = query.from(Card::class.java)
            if (shouldFilterByStats)
            {

                var secondaryField = "health"
                if (type == Type.WEAPON)
                    secondaryField = "durability"
                query
                        .where(cb.equal(root.get<Int>("attack"), attack))
                        .where(cb.equal(root.get<Int>(secondaryField), health))
            }
            query.where(cb.any(root.get<Array<String>>("mechanics").`in`(mechanics)))
        }
    }

    companion object
    {
        @JvmStatic
        private val attackOrHealthRegex: Regex = Regex("(\\d+)[\\s/](\\d+)")

        @JvmStatic
        private val spellDamage: Regex = Regex("$(\\d+)")

        @JvmStatic
        private val types = Type.values().map { it.name.toLowerCase() }
    }
}
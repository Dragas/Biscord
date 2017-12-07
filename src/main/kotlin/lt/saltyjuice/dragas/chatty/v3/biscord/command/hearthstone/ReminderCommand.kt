package lt.saltyjuice.dragas.chatty.v3.biscord.command.hearthstone

import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Type
import lt.saltyjuice.dragas.chatty.v3.biscord.joinToStrings
import lt.saltyjuice.dragas.chatty.v3.biscord.runIf
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.CardUtility
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.chatty.v3.discord.Settings
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import lt.saltyjuice.dragas.utility.kommander.main.Command
import org.hibernate.criterion.Subqueries
import org.hibernate.query.Query
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Subquery
import kotlin.streams.toList

@Name("remind-me")
class ReminderCommand : ProtectedDiscordCommand()
{
    override val requiredPermissions: Long = 0

    @Modifier("")
    @Description("args which are parsed")
    protected var defaultArg: String = ""

    protected val mechanics: List<String> by lazy()
    {
        CardUtility
                .mechanics
                .stream()
                .filter { defaultArg.contains(it, true) }
                .peek { defaultArg = defaultArg.replace(it, "", true) }
                .map { it.replace(" ", "_", true) }
                .map { it.toUpperCase() }
                .toList()
    }

    protected val rarity: String by lazy()
    {
        CardUtility
                .rarities
                .stream()
                .filter { defaultArg.contains(it, true) }
                .findFirst()
                .orElse("")
                .apply { defaultArg.replace(this, "", true) }
    }

    protected var shouldFilterByPrimaryStat = false
    protected var shouldFilterBySecondaryStat = false

    protected val primary: Int by lazy()
    {
        val groups = attackOrHealthRegex.find(defaultArg)!!.groupValues
        val attackString = groups[1]
        shouldFilterByPrimaryStat = attackString.isNotBlank()
        if (attackString.isNotBlank())
            attackString.toInt()
        else
            0
    }

    protected val secondary: Int by lazy()
    {
        val groups = attackOrHealthRegex.find(defaultArg)!!.groupValues
        val attackString = groups[2]
        shouldFilterBySecondaryStat = attackString.isNotBlank()
        if (attackString.isNotBlank())
            attackString.toInt()
        else
            0
    }

    protected val spellDamage: Int by lazy()
    {
        val groups = spellDamageRegex.find(defaultArg)!!.groupValues
        val attackString = groups[1]
        shouldFilterBySecondaryStat = attackString.isNotBlank()
        if (attackString.isNotBlank())
            attackString.toInt()
        else
            0
    }

    protected var type: Type? = null


    override fun onValidate(): Boolean
    {
        if (defaultArg.isBlank())
        {
            respondAsync("Expected some leads. Got nothing.")
            return false
        }
        return true
    }

    protected var clazz: String? = null

    override fun execute()
    {
        type = types
                .firstOrNull { defaultArg.contains(it, true) }
                ?.apply { defaultArg = defaultArg.replace(this, "", true) }
                ?.run(Type::valueOf)
        clazz = CardUtility
                .classes
                .firstOrNull { defaultArg.contains(it, true) }
                ?.apply { defaultArg = defaultArg.replace(this, "", true) }

        if (attackOrHealthRegex.containsMatchIn(defaultArg))
        {
            if (type == null) type = Type.MINION
            primary
            secondary
            defaultArg = defaultArg.replace(attackOrHealthRegex, "")
        }
        else if (spellDamageRegex.containsMatchIn(defaultArg))
        {
            spellDamage
            defaultArg = defaultArg.replace(spellDamageRegex, "")
            type = Type.SPELL
        }
        val results: List<Card> = CardUtility
                .getCollectable()
                .parallelStream()
                .runIf(rarity.isNotBlank()) { filter { it.rarity == rarity } }
                .runIf(type != null) { filter { it.type == type } }
                .runIf(mechanics.isNotEmpty()) { filter { it.mechanics.all(mechanics::contains) && mechanics.all(it.mechanics::contains) } }
                .runIf(shouldFilterByPrimaryStat) { filter { it.attack == primary } }
                .runIf(type == Type.WEAPON && shouldFilterBySecondaryStat) { filter { it.durability == secondary } }
                .runIf(type == Type.MINION && shouldFilterBySecondaryStat) { filter { it.health == secondary } }
                .runIf(type == Type.SPELL) { filter { it.text.contains("$$spellDamage") } }
                .runIf(clazz != null) { filter { it.playerClass.contains(clazz!!, true) } }
                .toList()
        if (results.isNotEmpty())
        {
            respond("I have found ${results.size} matches. Did you mean any of the following: ")
            results.parallelStream().map(Card::name).sorted().toList().joinToStrings(limit = Settings.MAX_MESSAGE_CONTENT_LENGTH).forEach(this::respond)
        }
        else
            respond("Nope. Doesn't ring a bell.")
    }

    companion object
    {
        @JvmStatic
        private val attackOrHealthRegex: Regex = Regex("(\\d+)?/(\\d+)?")

        @JvmStatic
        private val spellDamageRegex: Regex = Regex("\\$(\\d+)")

        @JvmStatic
        private val types = Type.values().map { it.name.toLowerCase() }
    }
}
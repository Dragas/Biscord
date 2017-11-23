package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.DeckWorker
import lt.saltyjuice.dragas.chatty.v3.discord.message.MessageBuilder
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name
import lt.saltyjuice.dragas.utility.kommander.main.Command
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Name("deckode")
@Description("Decodes deck codes into more human readable forms")
class DeckCommand : Command
{
    private lateinit var dw: DeckWorker

    @Modifier("")
    @JvmField
    @Description("Deck code encoded in base64 as seen on various sources")
    var kode: String = ""

    @Modifier("chid")
    @JvmField
    @Description("Redundant. Jeeves overrides this parameter anyways.")
    var chid: String = ""

    override fun validate(): Boolean
    {
        if (chid.isBlank())
            return false
        dw = DeckWorker(kode)
        return dw.isValid()
    }

    override fun execute()
    {
        val deck = dw.getAsDeck()
        var messageBuilder = MessageBuilder()
        deck
                .toList()
                .groupBy { Math.min(it.first.cost, 7) }
                .toMutableMap()
                .apply {
                    repeat(8)
                    {
                        if (!this.containsKey(it))
                            this[it] = listOf()
                    }
                }
                .toSortedMap()
                .forEach()
                { cost, cards ->
                    val costValue = if (cost == 7) "7+" else "$cost"
                    val count = cards.sumBy(Pair<Card, Int>::second)
                    messageBuilder.field("$costValue cost", "<:blu_square:380807906888646656>".repeat(count).plus("($count)"))
                }
        val costs = deck
                .map()
                {
                    val costs = it.key.getCraftingCost()
                    val regularCost = costs.first * it.value
                    val goldenCost = costs.second * it.value
                    Pair(regularCost, goldenCost)
                }
                .fold(Pair(0, 0))
                { acc, pair ->
                    acc + pair
                }
        messageBuilder
                .title("${dw.getClass().get().playerClass} deck")
                .description("Mode: ${dw.getFormat()}")
                .thumbnail(dw.getClass().get().artwork)
                .field("Regular cost", costs.first.toString())
                .field("Golden cost", costs.second.toString(), true)
        messageBuilder.send(chid, object : Callback<Message>
        {
            override fun onFailure(call: Call<Message>?, t: Throwable?)
            {

            }

            override fun onResponse(call: Call<Message>?, response: Response<Message>?)
            {
                messageBuilder = MessageBuilder()
                deck.toList()
                        .sortedBy { it.second }
                        .sortedBy { it.first.name }
                        .sortedBy { it.first.cost }
                        .forEach { (card, count) ->
                            messageBuilder.appendLine("# ${count}x (${card.cost}) ${card.name}")
                        }
                messageBuilder.send(chid)
            }
        })

    }
}

operator fun Pair<Int, Int>.plus(another: Pair<Int, Int>): Pair<Int, Int>
{
    val first = this.first + another.first
    val second = this.second + another.second
    return Pair(first, second)
}
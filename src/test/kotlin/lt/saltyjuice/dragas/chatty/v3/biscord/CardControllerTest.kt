package lt.saltyjuice.dragas.chatty.v3.biscord

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.discord.message.event.EventMessageCreate
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.InputStreamReader

@RunWith(JUnit4::class)
class CardControllerTest
{
    @Test
    fun isCardRequest()
    {
        val messageCreateEvent = EventMessageCreate()
        messageCreateEvent.data = Message().apply()
        {
            content = "card wrath"
        }
        Assert.assertTrue(cc.isCardRequest(messageCreateEvent))
    }

    @Test
    fun getsCardExactly()
    {
        cc.pushArguments(arrayOf("wrath"))
        cc.getFilteredForSingle(CardController.getCards().parallelStream()).findFirst().get()
    }

    @Test
    fun getsException()
    {
        cc.pushArguments(arrayOf("black lotus"))
        cc.getFilteredForSingle(CardController.getCards().parallelStream()).findFirst().get()
    }

    @Test
    fun getsManyCards()
    {
        cc.pushArguments(arrayOf("murloc"))
        Assert.assertTrue(cc.getFilteredForMany(CardController.getCards().parallelStream()).count() > 0)
    }

    @Test
    fun fallsBackToMany()
    {
        cc.pushArguments(arrayOf("murloc"))
        Assert.assertTrue(cc.filterCards().isNotEmpty())
    }

    @Test
    fun filtersForEntourage()
    {
        val count = CardController.getCollectable().parallelStream().filter { it.entourages.isNotEmpty() }.count()
        Assert.assertNotEquals(0L, count)
    }

    @Test
    fun entouragesMatch()
    {
        val collectables = CardController.getCollectable()
        collectables.forEach()
        {
            try
            {
                Assert.assertEquals(it.entourage.size, it.entourages.size)
            }
            catch (err: Throwable)
            {
                println(it.name)
                throw err
            }
        }
    }


    companion object
    {
        @JvmStatic
        private lateinit var cc: CardController

        @JvmStatic
        @BeforeClass
        fun init()
        {
            initializeCardController()
            cc = CardController()
        }

        @JvmStatic
        fun initializeCardController()
        {
            val gson = Gson()
            val cardArrayType = object : TypeToken<Set<Card>>()
            {}
            val json = Thread.currentThread().contextClassLoader.getResourceAsStream("cards.json")
            val jin = InputStreamReader(json)
            val array: Set<Card> = gson.fromJson(jin, cardArrayType.type)
            CardController.initialize(array)
        }
    }
}
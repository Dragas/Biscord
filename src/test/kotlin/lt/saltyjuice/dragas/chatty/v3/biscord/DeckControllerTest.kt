package lt.saltyjuice.dragas.chatty.v3.biscord

import lt.saltyjuice.dragas.chatty.v3.biscord.controller.DeckController
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.PlayerClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DeckControllerTest
{
    @Test
    fun canDecodeCards()
    {
        var count = 0
        val deck = dc.obtainDeck()
        deck.forEach {
            count += it.value
        }
        try
        {
            Assert.assertEquals(30, count)
        }
        catch (err: Throwable)
        {
            deck.forEach { t, u ->
                println(t.name + ": " + u)
            }
            throw  err
        }

    }

    @Test
    fun canDecodeClass()
    {
        Assert.assertEquals(PlayerClass.Druid, dc.obtainHeroClass())
    }

    @Test
    fun canDecodeVersion()
    {
        Assert.assertEquals(1, dc.obtainVersion())
    }

    @Test
    fun canDecodeFormat()
    {
        Assert.assertEquals(DeckController.Format.Standard, dc.obtainFormat())
    }

    @Test
    fun canDecodeNumberOfHeroes()
    {
        Assert.assertEquals(1, dc.obtainNumberOfHeroes())
    }

    companion object
    {
        @JvmStatic
        lateinit var dc: DeckController

        @JvmStatic
        val hash = "AAECAZICBvgMrqsClL0C+cACyccCmdMCDEBf/gHEBuQIvq4CtLsCy7wCz7wC3b4CoM0Ch84CAA=="

        @BeforeClass
        @JvmStatic
        fun init()
        {
            CardControllerTest.initializeCardController()
            dc = DeckController()
            Assert.assertTrue(dc.canDecode(hash))
            //dc.initializeDecoder()
            dc.decodeAsDeck()
        }
    }
}
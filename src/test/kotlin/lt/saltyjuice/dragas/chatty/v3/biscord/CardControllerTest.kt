package lt.saltyjuice.dragas.chatty.v3.biscord

import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CardControllerTest : MockCardController()
{
    @Test
    fun testFindsACard()
    {
        val card = CardController.getCardById(46643)
        Assert.assertTrue(card.isPresent)
    }

    companion object
    {
        @JvmStatic
        @BeforeClass
        fun init()
        {
            initializeCards()
        }
    }
}
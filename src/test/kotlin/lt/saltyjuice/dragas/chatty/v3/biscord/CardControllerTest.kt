package lt.saltyjuice.dragas.chatty.v3.biscord

import lt.saltyjuice.dragas.chatty.v3.biscord.command.CardCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.mock.MockCardController
import lt.saltyjuice.dragas.chatty.v3.biscord.mock.MockWorker
import lt.saltyjuice.dragas.chatty.v3.biscord.mock.MockWorkerBuilder
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

    @Test
    fun findsCardFromCommand()
    {
        val command = CardCommand()
        command.cardName("Jeeves")
        checkIfItFoundAnyCards(command)
    }

    @Test
    fun findsCardFromCommandViaWorker()
    {
        val worker = MockWorkerBuilder(CardCommand::class.java).build().second
        worker.execute("-n Jeeves")
        worker as MockWorker
        checkIfItFoundAnyCards(worker.lastInstance as CardCommand)
    }

    private fun checkIfItFoundAnyCards(worker: CardCommand)
    {
        val list = worker.getCardList()
        Assert.assertTrue(list.isNotEmpty())
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
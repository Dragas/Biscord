package lt.saltyjuice.dragas.chatty.v3.biscord

import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.mock.MockCardController
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.DeckWorker
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DeckWorkerTest : MockCardController()
{
    @Test
    fun decodesDeckCorrectly()
    {
        val worker = DeckWorker(deckHash)
        testWorker(worker)
    }

    @Test
    fun decodesDeckWithMultiplesCorrectly()
    {
        val hash = "AAEBAf0EAAABjBQe"
        val worker = DeckWorker(hash)
        testWorker(worker)
    }

    private fun testWorker(worker: DeckWorker)
    {
        Assert.assertTrue(worker.isValid())
        val cards = worker.getAsDeck()
        val clazz = worker.getClass()
        val cardSum = cards.map(Map.Entry<Card, Int>::value).sum()
        Assert.assertEquals(637, clazz.get().dbfId)
        Assert.assertEquals(30, cardSum)
        Assert.assertEquals(DeckWorker.Format.Wild, worker.getFormat())
    }

    companion object
    {
        private val deckHash = "AAEBAf0EBPoOnhCjtgKvwgINcbsClQOrBLQE5gSWBbkG9w2QEOMRjBSYxAIA"
        @BeforeClass
        @JvmStatic
        fun init()
        {
            initializeCards()
        }
    }
}
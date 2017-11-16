package lt.saltyjuice.dragas.chatty.v3.biscord

import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class HibernateTest
{
    @Test
    fun hibernateInitializes()
    {
        HibernateUtil.executeTransaction({ session ->
            val query = session.createQuery("from Card", Card::class.java)
            val result = query.resultList
            Assert.assertTrue(result.isEmpty())
        })
    }
}
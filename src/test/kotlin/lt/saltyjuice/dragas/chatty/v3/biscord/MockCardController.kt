package lt.saltyjuice.dragas.chatty.v3.biscord

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import lt.saltyjuice.dragas.chatty.v3.biscord.controller.CardController
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Card
import java.io.InputStreamReader

open class MockCardController
{
    companion object
    {
        @JvmStatic
        fun initializeCards()
        {
            val gson = Gson()
            val cl = Thread.currentThread().contextClassLoader
            val cardsJson = cl.getResourceAsStream("cards.json")
            val jsonreader = InputStreamReader(cardsJson)
            val token = object : TypeToken<Set<Card>>()
            {}.type
            val cards: Set<Card> = gson.fromJson(jsonreader, token)
            CardController.initialize(cards)
        }
    }
}
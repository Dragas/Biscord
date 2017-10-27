package lt.saltyjuice.dragas.chatty.v3.biscord

import lt.saltyjuice.dragas.chatty.v3.biscord.mock.MockConnectionController
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Message
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.Ready
import lt.saltyjuice.dragas.chatty.v3.discord.message.general.User
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UtilityTest
{
    @Test
    fun clearsMentionsCorrectly()
    {
        clearMentions("@")
    }

    @Test
    fun clearsNicknameMentionsCorrectly()
    {
        clearMentions("@!")
    }

    private fun clearMentions(separator: String)
    {
        val mdc = MockConnectionController()
        val user = User().apply { this.id = "1" }
        val ready = Ready().apply { this.user = user }
        mdc.onReady(ready)
        val message = Message().apply { this.content = "<$separator${user.id}>" }
        message.mentionedUsers.add(user)
        message.clearMyMentions()
        Assert.assertTrue(message.content.isEmpty())
        Assert.assertTrue(message.mentionedUsers.isEmpty())
    }

    @Test
    fun doesIfCorreclty()
    {
        true.doIf { Assert.assertTrue(true) }
    }

    @Test
    fun doesIfNotWorkOnFalse()
    {
        false.doIf { Assert.assertTrue(false) }
    }
}
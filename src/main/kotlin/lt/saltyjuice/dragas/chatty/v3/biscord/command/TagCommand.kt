package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.chatty.v3.biscord.command.discord.ProtectedDiscordCommand
import lt.saltyjuice.dragas.chatty.v3.biscord.entity.Tag
import lt.saltyjuice.dragas.chatty.v3.biscord.utility.HibernateUtil
import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.annotations.Name

@Name("tag")
@Description("used to tag hearthstone cards to their counterparts")
class TagCommand : ProtectedDiscordCommand()
{
    override val requiredPermissions: Long = 4

    @Modifier("t")
    private var target : String = ""

    @Modifier("")
    private var source : String = ""

    @Modifier("f")
    private var force : Boolean = false

    override fun onValidate(): Boolean
    {
        return target.isNotBlank() && source.isNotBlank()
    }

    override fun execute()
    {
        val tag = Tag(source, target)
        HibernateUtil.executeSimpleTransaction()
        { session ->
            val query = session.createQuery("from Tag where lower(key) = lower(:key)", Tag::class.java)
            query.setParameter("key", source)
            val results = query.resultList
            if(results.isEmpty() || force)
                session.saveOrUpdate(tag)
            else
                respond("There's already a tag for `$source`, which leads to `${tag.value}`. If you want to overwrite that use `tag $source -t $target -f`")
        }
    }
}
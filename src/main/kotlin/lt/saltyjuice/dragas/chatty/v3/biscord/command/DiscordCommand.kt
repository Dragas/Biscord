package lt.saltyjuice.dragas.chatty.v3.biscord.command

import lt.saltyjuice.dragas.utility.kommander.annotations.Description
import lt.saltyjuice.dragas.utility.kommander.annotations.Modifier
import lt.saltyjuice.dragas.utility.kommander.main.Command

abstract class DiscordCommand : Command
{
    @Modifier("chid")
    @JvmField
    @Description("Redundant. Jeeves overrides this parameter anyways.")
    var chid: String = ""


    @Modifier("user")
    @JvmField
    @Description("Redundant. Jeeves overrides this value with caller's ID")
    var userId : String = ""

}
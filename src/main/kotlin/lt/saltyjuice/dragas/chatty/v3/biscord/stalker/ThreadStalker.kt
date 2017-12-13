package lt.saltyjuice.dragas.chatty.v3.biscord.stalker

import lt.saltyjuice.dragas.chatty.v3.biscord.getenv
import lt.saltyjuice.dragas.utility.khan4.Khan
import lt.saltyjuice.dragas.utility.khan4.entity.Page
import kotlin.streams.toList
import lt.saltyjuice.dragas.utility.khan4.entity.Thread as KhanThread

class ThreadStalker(vararg listeners : (List<KhanThread>) -> Unit) : Stalker<List<KhanThread>>(*listeners)
{
    override val delayBetweenCalls: Long = getenv("stalk_delay", "900000").toLong()

    override fun getData(): List<KhanThread>
    {
        return (Khan
                .getCatalog("vg")
                .body() ?: listOf())
                .parallelStream()
                .map(Page<KhanThread>::threads)
                .flatMap(List<KhanThread>::stream)
                .toList()
    }
}
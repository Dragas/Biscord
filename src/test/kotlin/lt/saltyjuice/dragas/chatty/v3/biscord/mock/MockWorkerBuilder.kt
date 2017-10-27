package lt.saltyjuice.dragas.chatty.v3.biscord.mock

import lt.saltyjuice.dragas.utility.kommander.main.Command
import lt.saltyjuice.dragas.utility.kommander.worker.Worker
import lt.saltyjuice.dragas.utility.kommander.worker.WorkerBuilder

class MockWorkerBuilder(private val clazz: Class<out Command>) : WorkerBuilder(clazz)
{
    override fun build(): Pair<String, Worker>
    {
        val original = super.build()
        return Pair(original.first, MockWorker(clazz, modifiers))
    }
}
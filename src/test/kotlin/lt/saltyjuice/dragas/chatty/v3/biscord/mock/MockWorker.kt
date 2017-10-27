package lt.saltyjuice.dragas.chatty.v3.biscord.mock

import lt.saltyjuice.dragas.utility.kommander.main.Command
import lt.saltyjuice.dragas.utility.kommander.worker.Worker
import java.lang.reflect.Method

class MockWorker(private val clazz: Class<out Command>, methods: HashMap<String, Method>) : Worker(clazz, methods)
{
    override fun execute(line: String)
    {
        val commandInstance = clazz.newInstance()
        lastInstance = commandInstance
        line.split(defaultSeparator)
                .map { it.split(" ", limit = 2) }
                .forEach { parseModifier(commandInstance, it) }
        commandInstance.execute()
    }

    public lateinit var lastInstance: Command
}
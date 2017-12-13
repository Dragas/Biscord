package lt.saltyjuice.dragas.chatty.v3.biscord.stalker

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.selects.whileSelect

abstract class Stalker<T> : AutoCloseable
{
    protected val channel : BroadcastChannel<T> = BroadcastChannel(5)

    @set:Synchronized
    @get:Synchronized
    protected lateinit var job : Job

    protected open val shouldRestartOnCrash = true

    abstract val delayBetweenCalls : Long

    protected constructor()
    {

    }

    constructor(vararg listener: Listener<T>) : this()
    {
        listener.forEach(this::addListener)
    }

    constructor(vararg listener : (T) -> Unit) : this()
    {
        listener.forEach(this::addListener)
    }


    fun begin()
    {
        job = launch(Unconfined)
        {
            var crashed = false
            try
            {
                channel.send(getData())
            }
            catch (err : Exception)
            {
                err.printStackTrace()
                crashed = true
            }

            if(!crashed || shouldRestartOnCrash)
            {
                delay(delayBetweenCalls)
                begin()
            }
            else
                close()
        }
    }

    fun addListener(listener: Listener<T>)
    {
       launch(Unconfined)
       {
           channel.openSubscription().consumeEach(listener::onFind)
       }

    }

    fun addListener(listener : (T) -> Unit)
    {
        val actualListener = object : Listener<T>
        {
            override fun onFind(data: T)
            {
                listener(data)
            }
        }
        addListener(actualListener)
    }

    abstract fun getData() : T

    override fun close()
    {
        channel.close()
        job.cancel()
    }
    interface Listener<in T>
    {
        fun onFind(data : T)

    }
}
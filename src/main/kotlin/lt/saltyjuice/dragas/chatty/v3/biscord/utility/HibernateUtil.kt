package lt.saltyjuice.dragas.chatty.v3.biscord.utility

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.service.ServiceRegistry

object HibernateUtil
{
    @JvmStatic
    val sessionFactory: SessionFactory
    @JvmStatic
    private val serviceRegistry: ServiceRegistry

    init
    {
        serviceRegistry = StandardServiceRegistryBuilder()
                .configure(Thread.currentThread().contextClassLoader.getResource("hibernate.cfg.xml"))
                .build()
        val metadata = MetadataSources(serviceRegistry).buildMetadata()
        sessionFactory = metadata.buildSessionFactory()
    }

    @JvmOverloads
    fun <T> executeTransaction(transaction: ((Session) -> T), afterTransaction: ((Session, result: T) -> Unit)? = null): T
    {
        val session = sessionFactory.openSession()
        session.transaction.begin()
        try
        {
            val result = transaction(session)
            afterTransaction?.invoke(session, result)
            return result
        }
        catch (err: Exception)
        {
            session.transaction.rollback()
            throw err
        }
        finally
        {
            session.close()
        }
    }

    fun <T> executeDetachedTransaction(transaction: ((Session) -> T)): T
    {
        return executeTransaction(transaction)
        { session: Session, result: T ->
            if (result is Iterable<*>)
                result.forEach { detachFromSession(session, it) }
            else
                detachFromSession(session, result)
        }
    }

    private fun detachFromSession(session: Session, item: Any?)
    {
        item ?: return
        session.detach(item)
    }
}
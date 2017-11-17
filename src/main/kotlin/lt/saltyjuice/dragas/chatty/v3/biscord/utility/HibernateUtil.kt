package lt.saltyjuice.dragas.chatty.v3.biscord.utility

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.service.ServiceRegistry

object HibernateUtil
{
    @JvmStatic
    private val sessionFactory: SessionFactory
    @JvmStatic
    private val serviceRegistry: ServiceRegistry

    init
    {
        serviceRegistry = StandardServiceRegistryBuilder()
                .configure()
                .build()
        val metadata = MetadataSources(serviceRegistry).buildMetadata()
        sessionFactory = metadata.buildSessionFactory()
    }

    @JvmOverloads
    fun <T> executeTransaction(transaction: ((Session) -> T), afterTransaction: ((Session, result: T) -> Unit)? = null): T
    {
        val session = sessionFactory.openSession()
        val tx = session.transaction
        tx.begin()
        try
        {
            val result = transaction(session)
            afterTransaction?.invoke(session, result)
            return result
        }
        catch (err: Exception)
        {
            tx.rollback()
            throw err
        }
        finally
        {
            if (tx.isActive)
                tx.commit()
            session.close()
        }
    }

    @JvmStatic
    fun executeSimpleTransaction(transaction: ((session: Session) -> Unit), afterTransaction: ((Session) -> Unit)? = null)
    {
        executeTransaction(transaction, { session, _ -> afterTransaction?.invoke(session) })
    }

    fun <T> executeDetachedTransaction(transaction: ((session: Session) -> T)): T
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
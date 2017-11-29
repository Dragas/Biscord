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

    fun <T> executeTransaction(transaction: ((Session) -> T)): T
    {
        val session = sessionFactory.openSession()
        val tx = session.transaction
        tx.begin()
        try
        {
            val result = transaction(session)
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
    fun executeSimpleTransaction(transaction: ((session: Session) -> Unit))
    {
        executeTransaction(transaction)
    }
}
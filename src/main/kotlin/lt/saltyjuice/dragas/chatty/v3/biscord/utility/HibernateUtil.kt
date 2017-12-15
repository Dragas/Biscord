package lt.saltyjuice.dragas.chatty.v3.biscord.utility

import lt.saltyjuice.dragas.chatty.v3.biscord.runIf
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.service.ServiceRegistry
import java.net.URI

object HibernateUtil
{
    @JvmStatic
    private val sessionFactory: SessionFactory
    @JvmStatic
    private val serviceRegistry: ServiceRegistry

    init
    {
        val env = System.getenv("DATABASE_URL")
        var dbURL = env.replaceFirst("postgres:", "postgresql:").replaceFirst(Regex("/\\w+:?\\w*@"), "/")
        if(!dbURL.startsWith("jdbc:"))
            dbURL = "jdbc:" + dbURL
        val parsed = URI(env.replaceFirst("jdbc:", ""))
        val userInfo = parsed.userInfo.split(":").toList()
        serviceRegistry = StandardServiceRegistryBuilder()
                .configure()
                .applySetting("hibernate.connection.url", dbURL)
                .runIf(userInfo[0].isNotEmpty()) {applySetting("hibernate.connection.username", userInfo[0])}
                .runIf(userInfo.getOrNull(1)?.isNotEmpty() == true) {applySetting("hibernate.connection.password", userInfo[1])}
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
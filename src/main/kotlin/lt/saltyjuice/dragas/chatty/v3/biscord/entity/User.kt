package lt.saltyjuice.dragas.chatty.v3.biscord.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
open class User @JvmOverloads constructor(targetUserId: String = "")
{
    @Id
    open var id: String = targetUserId

    open var permissions: Long = 0

}
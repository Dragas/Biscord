package lt.saltyjuice.dragas.chatty.v3.biscord.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
open class User
{
    @Id
    var id : String = ""

    var permissions : Long = 0
}
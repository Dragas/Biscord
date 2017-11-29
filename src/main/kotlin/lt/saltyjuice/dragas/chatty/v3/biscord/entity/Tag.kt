package lt.saltyjuice.dragas.chatty.v3.biscord.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "tags")
class Tag @JvmOverloads constructor(@Id var key: String = "", var value: String = "")
{
}
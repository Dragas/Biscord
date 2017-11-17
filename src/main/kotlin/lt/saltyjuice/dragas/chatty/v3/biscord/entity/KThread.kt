package lt.saltyjuice.dragas.chatty.v3.biscord.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import lt.saltyjuice.dragas.utility.khan4.entity.Thread as KhanThread

@Entity
@Table(name = "threads")
class KThread()
{
    @Id
    var id: Long = -1

    constructor(khanthread: KhanThread) : this()
    {
        this.id = khanthread.postNumber
    }

}
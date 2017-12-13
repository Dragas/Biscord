package lt.saltyjuice.dragas.chatty.v3.biscord.entity

import lt.saltyjuice.dragas.utility.khan4.entity.Thread
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "known_threads")
open class KnownThread()
{
    @Id
    var id: Long = -1

    constructor(khanthread: Thread) : this()
    {
        this.id = khanthread.postNumber
    }
}
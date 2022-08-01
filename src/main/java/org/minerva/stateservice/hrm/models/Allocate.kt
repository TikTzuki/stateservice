package org.minerva.stateservice.hrm.models

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "allocate")
class Allocate(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @ManyToOne
    var user: User = User(),

    @ManyToOne
    var org: Org = Org(),

    @OneToOne
    var role: Role = Role(),
    var expire_date: LocalDateTime? = LocalDateTime.now()
)
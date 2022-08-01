package org.minerva.stateservice.hrm.models

import javax.persistence.*

@Entity
@Table(name = "org_user")
class User(
    @Id
    var username: String? = null,
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    var allocates: Set<Allocate> = HashSet()
) {
    constructor(username_: String) : this(username = username_)
}
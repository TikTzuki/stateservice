package org.minerva.stateservice.hrm.models

import javax.persistence.*

@Entity
@Table(name = "role")
class Role(
    @Id
    var code: String? = null,
    var name: String? = null,
    @ManyToMany
    @JoinTable(
        name = "role_permission",
        joinColumns = [JoinColumn(name = "role_code")],
        inverseJoinColumns = [JoinColumn(name = "permission_code")]
    )
    var permissions: Set<Permission> = HashSet()
) {
    constructor(code_: String?, name_: String?) : this(code = code_, name_)
}
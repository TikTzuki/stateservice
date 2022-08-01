package org.minerva.stateservice.hrm.models

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "permission")
class Permission(
    @Id
    var code: String? = null,
    var name: String? = null,
)
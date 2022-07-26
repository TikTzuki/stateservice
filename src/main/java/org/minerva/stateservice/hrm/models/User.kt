package org.minerva.stateservice.hrm.models

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "org_user")
class User(
    @Id
    var username: String? = null,
)
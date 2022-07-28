package org.minerva.stateservice.hrm.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "org_allocate")
class OrgAllocate(
    @Id
    @GeneratedValue
    var id: Long? = null,
    var username: String? = null,
    var orgId: Long? = null,
)
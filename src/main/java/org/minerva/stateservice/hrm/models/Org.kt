package org.minerva.stateservice.hrm.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "org")
class Org(
    @Id
    @GeneratedValue
    var id: Long? = null,
    var ancestry: String? = null,
    var data: String? = null,
){
    constructor(ancestry: String?, data: String?) : this(null, ancestry, data) {
    }
}
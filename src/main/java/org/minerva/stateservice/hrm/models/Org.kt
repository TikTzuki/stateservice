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
    var data: String,
) {
    constructor(ancestry: String?, data: String) : this(null, ancestry, data) {
    }

    constructor() : this(null, null, "{}") {
    }

    fun getPrevGatewayId(): String {
        return String.format("gt-%s", ancestry?.replace("/", "-"))
    }

    fun getNextGatewayId(): String {
        return String.format("gt-%s%s-", if (ancestry != null) ancestry?.replace("/", "-") else "", id)
    }

    fun getTaskId(): String {
        return String.format("org-%s", id);
    }
}
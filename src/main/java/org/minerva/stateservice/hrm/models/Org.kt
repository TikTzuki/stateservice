package org.minerva.stateservice.hrm.models

import javax.persistence.*


@Entity
@Table(name = "org")
class Org(
    @Id
    var id: String = "000",
    var ancestry: String? = null,
    var data: String = "{}",
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "org")
    var allocates: List<Allocate> = ArrayList(),
) {
    constructor(data_: String) : this(data = data_) {}

    constructor(id_: String, ancestry_: String?, data_: String) : this(id = id_, ancestry_, data_) {}

    companion object {
        @JvmStatic
        fun fromTask(id: String, ancestry: String?, data: String): Org {
            return Org(id.replace("org-", ""), ancestry, data)
        }
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
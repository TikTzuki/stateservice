package org.minerva.stateservice.hrm.models

import java.sql.Blob
import javax.persistence.*

@Entity
@Table(name = "BPMN")
class BPMN(
    @Id
    @GeneratedValue
    var id: Long? = null,
    @Lob
    var content: Blob? = null
)
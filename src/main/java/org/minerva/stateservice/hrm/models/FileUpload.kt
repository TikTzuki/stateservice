package org.minerva.stateservice.hrm.models

import java.time.LocalDateTime
import java.util.*

class FileUpload(
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    var createdBy: String,
    var updatedBy: String,
    var type: Int,
    var uuid: UUID,
    var name: String,
    var contentType: String,
    var size: Int,
    var version: Float,
    var fileUrl: String? = null,
)
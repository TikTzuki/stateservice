package org.minerva.stateservice.hrm.user

import org.minerva.stateservice.hrm.models.Allocate
import org.minerva.stateservice.hrm.models.User
import org.springframework.http.ResponseEntity

interface UserController {
    fun allocate(allocate: Allocate): ResponseEntity<*>
    fun createUser(user: User)
}
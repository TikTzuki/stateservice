package org.minerva.stateservice.hrm.user

import org.minerva.stateservice.hrm.models.Allocate
import org.minerva.stateservice.hrm.models.User

interface UserService {
    fun allocate(allocate: Allocate)
    fun createUser(user: User)
}
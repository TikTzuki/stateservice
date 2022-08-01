package org.minerva.stateservice.hrm.repos

import org.minerva.stateservice.hrm.models.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepos : JpaRepository<Role, String>
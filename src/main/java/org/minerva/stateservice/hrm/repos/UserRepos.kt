package org.minerva.stateservice.hrm.repos

import org.minerva.stateservice.hrm.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepos : JpaRepository<User?, Long?>
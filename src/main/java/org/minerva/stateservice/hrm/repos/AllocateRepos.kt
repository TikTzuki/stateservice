package org.minerva.stateservice.hrm.repos

import org.minerva.stateservice.hrm.models.Allocate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AllocateRepos : JpaRepository<Allocate, Long>
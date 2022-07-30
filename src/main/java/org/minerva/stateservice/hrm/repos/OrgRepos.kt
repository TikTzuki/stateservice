package org.minerva.stateservice.hrm.repos

import org.minerva.stateservice.hrm.models.Org
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrgRepos : JpaRepository<Org?, Long?> {
    fun findByAncestry(ancestry: String?): List<Org>
}
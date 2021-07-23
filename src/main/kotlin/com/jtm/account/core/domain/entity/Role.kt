package com.jtm.account.core.domain.entity

import com.jtm.account.core.domain.dto.RoleDto
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("roles")
data class Role(
    @Id val id: UUID = UUID.randomUUID(),
    var name: String,
    var priority: Int
) {

    fun update(role: RoleDto): Role {
        this.name = role.name
        this.priority = role.priority
        return this
    }
}

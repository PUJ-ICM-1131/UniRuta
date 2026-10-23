package com.uniruta.app.data.mock

import com.uniruta.app.data.model.MockUser
import com.uniruta.app.data.model.UserRole

object MockAccounts {

    val all = listOf(
        MockUser(
            id = "est-001",
            name = "Ana Gómez",
            email = "estudiante@uniruta.com",
            role = UserRole.STUDENT
        ),
        MockUser(
            id = "con-001",
            name = "Carlos Medina",
            email = "conductor@uniruta.com",
            role = UserRole.DRIVER
        ),
        MockUser(
            id = "adm-001",
            name = "Laura Peña",
            email = "admin@uniruta.com",
            role = UserRole.ADMIN
        )
    )

    fun findBy(role: UserRole): MockUser = all.first { it.role == role }
}

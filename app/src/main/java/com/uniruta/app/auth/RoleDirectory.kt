package com.uniruta.app.auth

import com.uniruta.app.data.model.UserRole

data class RoleAssignment(
    val role: UserRole,
    val displayName: String? = null
)

class RoleDirectory(private val assignments: Map<String, RoleAssignment>) {

    val size: Int
        get() = assignments.size

    fun assignmentFor(email: String?): RoleAssignment? =
        email?.trim()?.lowercase()?.let(assignments::get)

    companion object {

        private val placeholders = setOf("", "NONE", "DEFAULT_API_KEY")

        fun parse(raw: String?): RoleDirectory {
            val value = raw?.trim().orEmpty()
            if (value.uppercase() in placeholders) return RoleDirectory(emptyMap())

            val entries = value.split(',', ';')
                .mapNotNull { entry -> parseEntry(entry.trim()) }
                .toMap()
            return RoleDirectory(entries)
        }

        private fun parseEntry(entry: String): Pair<String, RoleAssignment>? {
            val parts = entry.split(':').map { it.trim() }
            if (parts.size < 2) return null
            val email = parts[0].lowercase()
            if (!isValidEmail(email)) return null
            val role = roleFor(parts[1]) ?: return null
            val name = parts.getOrNull(2)?.takeIf { it.isNotBlank() }
            return email to RoleAssignment(role, name)
        }

        private fun roleFor(value: String): UserRole? = when (value.uppercase()) {
            "STUDENT", "ESTUDIANTE" -> UserRole.STUDENT
            "DRIVER", "CONDUCTOR" -> UserRole.DRIVER
            "ADMIN", "ADMINISTRADOR" -> UserRole.ADMIN
            else -> null
        }
    }
}

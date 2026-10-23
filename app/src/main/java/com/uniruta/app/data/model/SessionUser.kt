package com.uniruta.app.data.model

enum class AuthMode(val label: String) {
    FIREBASE("Cuenta Firebase"),
    DEMO("Modo demostración")
}

data class SessionUser(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val mode: AuthMode
) {
    val key: String
        get() = "${mode.name.lowercase()}:$id"
}

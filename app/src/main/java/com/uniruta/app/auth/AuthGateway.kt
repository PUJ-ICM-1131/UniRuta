package com.uniruta.app.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class AuthIdentity(
    val uid: String,
    val email: String?,
    val displayName: String?
)

enum class AuthError(val message: String) {
    INVALID_EMAIL("El correo no tiene un formato válido."),
    WRONG_PASSWORD("La contraseña es incorrecta."),
    USER_NOT_FOUND("No existe una cuenta registrada con ese correo."),
    INVALID_CREDENTIALS("El correo o la contraseña no son correctos."),
    USER_DISABLED("Esta cuenta está deshabilitada. Contacta al administrador."),
    NETWORK("No hay conexión a internet. Verifica tu red e inténtalo de nuevo."),
    TOO_MANY_REQUESTS("Se realizaron demasiados intentos. Espera unos minutos e inténtalo de nuevo."),
    NOT_CONFIGURED("La autenticación con Firebase todavía no está configurada en esta compilación. Puedes usar el acceso de demostración."),
    UNEXPECTED("No fue posible iniciar sesión. Inténtalo de nuevo.")
}

sealed interface SignInResult {
    data class Success(val identity: AuthIdentity) : SignInResult
    data class Failure(val error: AuthError) : SignInResult
}

interface AuthGateway {

    val isConfigured: Boolean

    fun identityChanges(): Flow<AuthIdentity?>

    suspend fun signIn(email: String, password: String): SignInResult

    fun signOut()

    object Unavailable : AuthGateway {
        override val isConfigured: Boolean = false
        override fun identityChanges(): Flow<AuthIdentity?> = flowOf(null)
        override suspend fun signIn(email: String, password: String): SignInResult =
            SignInResult.Failure(AuthError.NOT_CONFIGURED)
        override fun signOut() = Unit
    }
}

private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun isValidEmail(value: String): Boolean = emailPattern.matches(value.trim())

fun authErrorForCode(code: String?): AuthError = when (code) {
    "ERROR_INVALID_EMAIL" -> AuthError.INVALID_EMAIL
    "ERROR_WRONG_PASSWORD" -> AuthError.WRONG_PASSWORD
    "ERROR_USER_NOT_FOUND" -> AuthError.USER_NOT_FOUND
    "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_LOGIN_CREDENTIALS" -> AuthError.INVALID_CREDENTIALS
    "ERROR_USER_DISABLED" -> AuthError.USER_DISABLED
    "ERROR_TOO_MANY_REQUESTS" -> AuthError.TOO_MANY_REQUESTS
    "ERROR_NETWORK_REQUEST_FAILED" -> AuthError.NETWORK
    "ERROR_API_NOT_AVAILABLE", "ERROR_APP_NOT_AUTHORIZED", "ERROR_INVALID_API_KEY", "ERROR_OPERATION_NOT_ALLOWED" ->
        AuthError.NOT_CONFIGURED
    else -> AuthError.UNEXPECTED
}

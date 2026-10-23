package com.uniruta.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uniruta.app.UniRutaApplication
import com.uniruta.app.auth.AuthError
import com.uniruta.app.auth.AuthGateway
import com.uniruta.app.auth.AuthIdentity
import com.uniruta.app.auth.RoleDirectory
import com.uniruta.app.auth.SignInResult
import com.uniruta.app.auth.isValidEmail
import com.uniruta.app.data.mock.MockAccounts
import com.uniruta.app.data.model.AuthMode
import com.uniruta.app.data.model.SessionUser
import com.uniruta.app.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val session: SessionUser? = null,
    val isRestoring: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val unassignedEmail: String? = null,
    val firebaseConfigured: Boolean = false
) {
    val canSubmit: Boolean
        get() = !isLoading && !isRestoring && email.isNotBlank() && password.isNotBlank()
}

class AuthViewModel(
    private val gateway: AuthGateway,
    private val roleDirectory: RoleDirectory
) : ViewModel() {

    private val _uiState = MutableStateFlow(signedOutState(restoring = gateway.isConfigured))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        if (gateway.isConfigured) observeIdentity()
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (!state.canSubmit || state.session != null) return
        val email = state.email.trim()
        val failure = when {
            !isValidEmail(email) -> AuthError.INVALID_EMAIL
            !gateway.isConfigured -> AuthError.NOT_CONFIGURED
            else -> null
        }
        if (failure != null) {
            _uiState.update { it.copy(errorMessage = failure.message, unassignedEmail = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, unassignedEmail = null) }
        viewModelScope.launch {
            when (val result = gateway.signIn(email, state.password)) {
                is SignInResult.Success -> {
                    val current = _uiState.value
                    val alreadyHandled = current.session?.id == result.identity.uid || current.unassignedEmail != null
                    if (alreadyHandled) {
                        _uiState.update { it.copy(isLoading = false) }
                    } else {
                        applyIdentity(result.identity)
                    }
                }
                is SignInResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, password = "", errorMessage = result.error.message)
                }
            }
        }
    }

    fun loginAs(role: UserRole) {
        val state = _uiState.value
        if (state.isLoading || state.isRestoring || state.session != null) return
        val account = MockAccounts.findBy(role)
        _uiState.value = signedOutState(restoring = false).copy(
            session = SessionUser(
                id = account.id,
                name = account.name,
                email = account.email,
                role = account.role,
                mode = AuthMode.DEMO
            )
        )
    }

    fun logout() {
        if (_uiState.value.session?.mode == AuthMode.FIREBASE) gateway.signOut()
        _uiState.value = signedOutState(restoring = false)
    }

    fun dismissUnassignedNotice() {
        _uiState.update { it.copy(unassignedEmail = null) }
    }

    private fun observeIdentity() {
        viewModelScope.launch {
            gateway.identityChanges()
                .catch { emit(null) }
                .collect(::onIdentityChanged)
        }
    }

    private fun onIdentityChanged(identity: AuthIdentity?) {
        val current = _uiState.value
        when {
            identity == null -> _uiState.update { state ->
                state.copy(
                    isRestoring = false,
                    session = state.session?.takeIf { it.mode == AuthMode.DEMO }
                )
            }
            current.session?.mode == AuthMode.DEMO -> Unit
            current.session?.id == identity.uid -> _uiState.update { it.copy(isRestoring = false) }
            current.session == null && current.unassignedEmail == (identity.email ?: identity.uid) -> Unit
            else -> applyIdentity(identity)
        }
    }

    private fun applyIdentity(identity: AuthIdentity) {
        val assignment = roleDirectory.assignmentFor(identity.email)
        if (assignment == null) {
            _uiState.update {
                it.copy(
                    isRestoring = false,
                    isLoading = false,
                    session = null,
                    password = "",
                    errorMessage = null,
                    unassignedEmail = identity.email ?: identity.uid
                )
            }
            gateway.signOut()
            return
        }

        val session = SessionUser(
            id = identity.uid,
            name = identity.displayName
                ?: assignment.displayName
                ?: identity.email?.substringBefore('@')
                ?: "Usuario",
            email = identity.email.orEmpty(),
            role = assignment.role,
            mode = AuthMode.FIREBASE
        )
        _uiState.update {
            it.copy(
                isRestoring = false,
                isLoading = false,
                session = session,
                email = "",
                password = "",
                errorMessage = null,
                unassignedEmail = null
            )
        }
    }

    private fun signedOutState(restoring: Boolean) = AuthUiState(
        isRestoring = restoring,
        firebaseConfigured = gateway.isConfigured
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as UniRutaApplication
                AuthViewModel(
                    gateway = application.container.authGateway,
                    roleDirectory = application.container.roleDirectory
                )
            }
        }
    }
}

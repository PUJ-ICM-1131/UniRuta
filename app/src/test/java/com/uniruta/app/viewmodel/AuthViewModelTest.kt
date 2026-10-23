package com.uniruta.app.viewmodel

import com.uniruta.app.auth.AuthError
import com.uniruta.app.auth.AuthGateway
import com.uniruta.app.auth.AuthIdentity
import com.uniruta.app.auth.RoleDirectory
import com.uniruta.app.auth.SignInResult
import com.uniruta.app.data.model.AuthMode
import com.uniruta.app.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private class FakeAuthGateway(override val isConfigured: Boolean = true) : AuthGateway {
        val identities = MutableSharedFlow<AuthIdentity?>(replay = 1)
        var nextResult: SignInResult = SignInResult.Failure(AuthError.INVALID_CREDENTIALS)
        var signInCalls = 0
        var signOutCalls = 0

        override fun identityChanges(): Flow<AuthIdentity?> = identities

        override suspend fun signIn(email: String, password: String): SignInResult {
            signInCalls++
            val result = nextResult
            if (result is SignInResult.Success) identities.tryEmit(result.identity)
            return result
        }

        override fun signOut() {
            signOutCalls++
            identities.tryEmit(null)
        }
    }

    private val dispatcher = UnconfinedTestDispatcher()
    private val directory = RoleDirectory.parse("laura@javeriana.edu.co:ADMIN:Laura Peña,ana@javeriana.edu.co:STUDENT")
    private val laura = AuthIdentity(uid = "uid-laura", email = "laura@javeriana.edu.co", displayName = null)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun demoAccessWorksWithoutFirebaseAndIsLabelledAsDemo() {
        val gateway = FakeAuthGateway(isConfigured = false)
        val viewModel = AuthViewModel(gateway, directory)

        assertFalse(viewModel.uiState.value.isRestoring)
        viewModel.loginAs(UserRole.DRIVER)

        val session = viewModel.uiState.value.session!!
        assertEquals(AuthMode.DEMO, session.mode)
        assertEquals(UserRole.DRIVER, session.role)
        assertEquals("Carlos Medina", session.name)
        assertEquals("", viewModel.uiState.value.password)
        assertEquals(0, gateway.signInCalls)
    }

    @Test
    fun emailLoginWithoutFirebaseExplainsTheMissingConfiguration() {
        val gateway = FakeAuthGateway(isConfigured = false)
        val viewModel = AuthViewModel(gateway, directory)

        viewModel.onEmailChange("laura@javeriana.edu.co")
        viewModel.onPasswordChange("secreto")
        viewModel.login()

        assertNull(viewModel.uiState.value.session)
        assertEquals(AuthError.NOT_CONFIGURED.message, viewModel.uiState.value.errorMessage)
        assertEquals(0, gateway.signInCalls)
    }

    @Test
    fun invalidEmailIsRejectedBeforeCallingFirebase() = runTest {
        val gateway = FakeAuthGateway()
        val viewModel = AuthViewModel(gateway, directory)
        gateway.identities.emit(null)

        viewModel.onEmailChange("laura")
        viewModel.onPasswordChange("secreto")
        viewModel.login()

        assertEquals(AuthError.INVALID_EMAIL.message, viewModel.uiState.value.errorMessage)
        assertEquals(0, gateway.signInCalls)
    }

    @Test
    fun failedLoginShowsMessageAndClearsPassword() = runTest {
        val gateway = FakeAuthGateway()
        val viewModel = AuthViewModel(gateway, directory)
        gateway.identities.emit(null)
        gateway.nextResult = SignInResult.Failure(AuthError.WRONG_PASSWORD)

        viewModel.onEmailChange("laura@javeriana.edu.co")
        viewModel.onPasswordChange("incorrecta")
        viewModel.login()

        val state = viewModel.uiState.value
        assertNull(state.session)
        assertFalse(state.isLoading)
        assertEquals(AuthError.WRONG_PASSWORD.message, state.errorMessage)
        assertEquals("", state.password)
        assertEquals("laura@javeriana.edu.co", state.email)
    }

    @Test
    fun successfulLoginUsesTheConfiguredRole() = runTest {
        val gateway = FakeAuthGateway()
        val viewModel = AuthViewModel(gateway, directory)
        gateway.identities.emit(null)
        gateway.nextResult = SignInResult.Success(laura)

        viewModel.onEmailChange("laura@javeriana.edu.co")
        viewModel.onPasswordChange("secreto")
        viewModel.login()

        val state = viewModel.uiState.value
        assertEquals(AuthMode.FIREBASE, state.session?.mode)
        assertEquals(UserRole.ADMIN, state.session?.role)
        assertEquals("Laura Peña", state.session?.name)
        assertEquals("firebase:uid-laura", state.session?.key)
        assertEquals("", state.password)
        assertEquals("", state.email)
    }

    @Test
    fun accountWithoutRoleIsSignedOutAndNeverGetsAccess() = runTest {
        val gateway = FakeAuthGateway()
        val viewModel = AuthViewModel(gateway, directory)
        gateway.identities.emit(null)
        gateway.nextResult = SignInResult.Success(
            AuthIdentity(uid = "uid-x", email = "desconocido@javeriana.edu.co", displayName = "Intruso")
        )

        viewModel.onEmailChange("desconocido@javeriana.edu.co")
        viewModel.onPasswordChange("secreto")
        viewModel.login()

        val state = viewModel.uiState.value
        assertNull(state.session)
        assertEquals("desconocido@javeriana.edu.co", state.unassignedEmail)
        assertEquals(1, gateway.signOutCalls)

        viewModel.dismissUnassignedNotice()
        assertNull(viewModel.uiState.value.unassignedEmail)
    }

    @Test
    fun existingFirebaseSessionIsRestoredAfterTheFirstAuthState() = runTest {
        val gateway = FakeAuthGateway()
        val viewModel = AuthViewModel(gateway, directory)

        assertTrue(viewModel.uiState.value.isRestoring)
        assertNull(viewModel.uiState.value.session)

        gateway.identities.emit(laura)

        assertFalse(viewModel.uiState.value.isRestoring)
        assertEquals(UserRole.ADMIN, viewModel.uiState.value.session?.role)
    }

    @Test
    fun restorationWithoutSessionEndsSignedOut() = runTest {
        val gateway = FakeAuthGateway()
        val viewModel = AuthViewModel(gateway, directory)

        gateway.identities.emit(null)

        assertFalse(viewModel.uiState.value.isRestoring)
        assertNull(viewModel.uiState.value.session)
    }

    @Test
    fun logoutClearsTheSessionAndSignsOutOfFirebase() = runTest {
        val gateway = FakeAuthGateway()
        val viewModel = AuthViewModel(gateway, directory)
        gateway.identities.emit(laura)

        viewModel.logout()

        assertNull(viewModel.uiState.value.session)
        assertFalse(viewModel.uiState.value.isRestoring)
        assertEquals(1, gateway.signOutCalls)
    }

    @Test
    fun demoLogoutDoesNotTouchFirebaseAndExternalSignOutEndsFirebaseSessions() = runTest {
        val gateway = FakeAuthGateway()
        val viewModel = AuthViewModel(gateway, directory)
        gateway.identities.emit(null)

        viewModel.loginAs(UserRole.STUDENT)
        gateway.identities.emit(null)
        assertEquals(AuthMode.DEMO, viewModel.uiState.value.session?.mode)
        viewModel.logout()
        assertEquals(0, gateway.signOutCalls)

        gateway.identities.emit(laura)
        assertEquals(AuthMode.FIREBASE, viewModel.uiState.value.session?.mode)
        gateway.identities.emit(null)
        assertNull(viewModel.uiState.value.session)
    }
}

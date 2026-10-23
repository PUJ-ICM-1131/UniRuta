package com.uniruta.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.uniruta.app.data.model.UserRole
import com.uniruta.app.notifications.NotificationTarget
import com.uniruta.app.ui.screens.auth.LoginScreen
import com.uniruta.app.ui.screens.auth.SessionSplash
import com.uniruta.app.viewmodel.AdminViewModel
import com.uniruta.app.viewmodel.AuthViewModel
import com.uniruta.app.viewmodel.DriverViewModel
import com.uniruta.app.viewmodel.EmergencyContactViewModel
import com.uniruta.app.viewmodel.StudentViewModel

@Composable
fun UniRutaApp(
    pendingTarget: NotificationTarget?,
    onTargetHandled: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    studentViewModel: StudentViewModel = viewModel(factory = StudentViewModel.Factory),
    driverViewModel: DriverViewModel = viewModel(factory = DriverViewModel.Factory),
    adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory),
    contactViewModel: EmergencyContactViewModel = viewModel(factory = EmergencyContactViewModel.Factory)
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val studentState = studentViewModel.uiState.collectAsStateWithLifecycle()
    val driverState = driverViewModel.uiState.collectAsStateWithLifecycle()
    val adminState = adminViewModel.uiState.collectAsStateWithLifecycle()
    val contactState = contactViewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(Login)
    val session = authState.session

    LifecycleStartEffect(driverViewModel) {
        driverViewModel.onAppVisibilityChanged(true)
        onStopOrDispose { driverViewModel.onAppVisibilityChanged(false) }
    }

    LaunchedEffect(session?.role) {
        driverViewModel.onMonitoringAllowedChanged(session?.role == UserRole.DRIVER)
    }

    LaunchedEffect(session?.key) {
        contactViewModel.bind(session?.key)
    }

    LaunchedEffect(authState.isRestoring, session?.key, pendingTarget) {
        if (authState.isRestoring) return@LaunchedEffect
        val role = session?.role
        val target = pendingTarget
        if (role != null && target != null) {
            onTargetHandled()
            if (target.role == role) {
                backStack.replaceWith(
                    target.backStackFor(studentState.value, driverState.value, adminState.value)
                )
                return@LaunchedEffect
            }
        }
        if (!backStack.isValidFor(role)) {
            backStack.replaceWith(listOf(role?.let(::homeDestinationFor) ?: Login))
        }
    }

    val renderable = !authState.isRestoring && backStack.all { key ->
        key.requiredRole().let { it == null || it == session?.role }
    }
    if (!renderable) {
        SessionSplash(modifier = modifier)
        return
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Login> {
                LoginScreen(
                    state = authState,
                    demoHasChanges = studentState.value.hasScenarioChanges ||
                        driverState.value.hasScenarioChanges ||
                        adminState.value.hasScenarioChanges,
                    onEmailChange = authViewModel::onEmailChange,
                    onPasswordChange = authViewModel::onPasswordChange,
                    onSubmit = authViewModel::login,
                    onDemoLogin = authViewModel::loginAs,
                    onDismissUnassigned = authViewModel::dismissUnassignedNotice,
                    onResetDemo = {
                        studentViewModel.reset()
                        driverViewModel.reset()
                        adminViewModel.reset()
                    }
                )
            }

            studentEntries(
                uiState = studentState,
                contactState = contactState,
                viewModel = studentViewModel,
                contactViewModel = contactViewModel,
                user = session,
                onLogout = authViewModel::logout,
                backStack = backStack
            )

            driverEntries(
                uiState = driverState,
                viewModel = driverViewModel,
                user = session,
                onLogout = authViewModel::logout,
                backStack = backStack
            )

            adminEntries(
                uiState = adminState,
                viewModel = adminViewModel,
                user = session,
                onLogout = authViewModel::logout,
                backStack = backStack
            )
        }
    )
}

private fun NavBackStack<NavKey>.replaceWith(keys: List<NavKey>) {
    if (toList() == keys) return
    clear()
    addAll(keys)
}

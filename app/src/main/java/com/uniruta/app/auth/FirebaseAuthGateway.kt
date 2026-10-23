package com.uniruta.app.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirebaseAuthGateway(context: Context) : AuthGateway {

    private val auth: FirebaseAuth? = resolveAuth(context.applicationContext)

    override val isConfigured: Boolean
        get() = auth != null

    override fun identityChanges(): Flow<AuthIdentity?> {
        val firebaseAuth = auth ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { source ->
                trySend(source.currentUser?.toIdentity())
            }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }
    }

    override suspend fun signIn(email: String, password: String): SignInResult {
        val firebaseAuth = auth ?: return SignInResult.Failure(AuthError.NOT_CONFIGURED)
        return try {
            val user = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await().user
            if (user == null) {
                SignInResult.Failure(AuthError.UNEXPECTED)
            } else {
                SignInResult.Success(user.toIdentity())
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: FirebaseAuthException) {
            SignInResult.Failure(authErrorForCode(error.errorCode))
        } catch (error: FirebaseNetworkException) {
            SignInResult.Failure(AuthError.NETWORK)
        } catch (error: FirebaseTooManyRequestsException) {
            SignInResult.Failure(AuthError.TOO_MANY_REQUESTS)
        } catch (error: Exception) {
            SignInResult.Failure(AuthError.UNEXPECTED)
        }
    }

    override fun signOut() {
        auth?.signOut()
    }

    private fun FirebaseUser.toIdentity() = AuthIdentity(
        uid = uid,
        email = email,
        displayName = displayName?.takeIf { it.isNotBlank() }
    )

    private companion object {
        fun resolveAuth(context: Context): FirebaseAuth? = try {
            val app = FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
            app?.let(FirebaseAuth::getInstance)
        } catch (error: IllegalStateException) {
            null
        } catch (error: IllegalArgumentException) {
            null
        }
    }
}

package com.example.data.cloud

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

sealed interface GoogleSignInResult {
    data class Success(val user: FirebaseUser) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Error(val message: String) : GoogleSignInResult
}

class AuthRepository(
    private val authProvider: () -> FirebaseAuth? = { runCatching { Firebase.auth }.getOrNull() }
) {
    constructor(auth: FirebaseAuth) : this({ auth })

    private fun authOrNull(): FirebaseAuth? = runCatching { authProvider() }.getOrNull()

    fun currentUser(): FirebaseUser? = runCatching { authOrNull()?.currentUser }.getOrNull()

    fun authStateFlow(): Flow<FirebaseUser?> {
        val firebaseAuth = authOrNull() ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { stateAuth ->
                trySend(runCatching { stateAuth.currentUser }.getOrNull())
            }
            runCatching { firebaseAuth.addAuthStateListener(listener) }
                .onFailure {
                    trySend(null)
                    close()
                    return@callbackFlow
                }
            awaitClose { runCatching { firebaseAuth.removeAuthStateListener(listener) } }
        }
    }

    suspend fun attemptAutoSignIn(context: Context): Boolean {
        val firebaseAuth = authOrNull() ?: return false
        if (runCatching { firebaseAuth.currentUser }.getOrNull() != null) return true
        val clientId = runCatching { context.getString(R.string.default_web_client_id) }.getOrNull()
            ?: return false
        if (clientId.isBlank()) return false

        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val credential = credentialManager.getCredential(context, request).credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
                true
            } else {
                false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            Log.w(TAG, "Auto sign-in skipped safely: ${t.message}")
            false
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): GoogleSignInResult {
        val firebaseAuth = authOrNull()
            ?: return GoogleSignInResult.Error("Cloud authentication service is unavailable on this device right now.")

        val clientId = try {
            activityContext.getString(R.string.default_web_client_id)
        } catch (_: Throwable) {
            return GoogleSignInResult.Error("Google Sign-In configuration missing: default_web_client_id not found.")
        }

        return try {
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()
            val credentialManager = CredentialManager.create(activityContext)
            val credential = credentialManager.getCredential(activityContext, request).credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authResult = firebaseAuth.signInWithCredential(
                    GoogleAuthProvider.getCredential(idToken, null)
                ).await()
                val user = authResult.user
                    ?: return GoogleSignInResult.Error("Google sign-in returned no Firebase user.")
                GoogleSignInResult.Success(user)
            } else {
                GoogleSignInResult.Error("Unexpected credential type received.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            GoogleSignInResult.Cancelled
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.w(TAG, "No Google account available for Credential Manager: ${e.message}")
            GoogleSignInResult.Error("No signed-in Google account found on this device. Please add a Google account in Settings and try again.")
        } catch (e: androidx.credentials.exceptions.GetCredentialProviderConfigurationException) {
            Log.w(TAG, "Credential provider unavailable: ${e.message}")
            GoogleSignInResult.Error("Google Sign-In service is unavailable or disabled on this device.")
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            Log.e(TAG, "Google Sign-In failed", t)
            GoogleSignInResult.Error(t.localizedMessage ?: "Google Sign-In failed on this device.")
        }
    }

    suspend fun signOut(context: Context) {
        withContext(NonCancellable) {
            try {
                withTimeoutOrNull(2500L) {
                    CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to clear Credential Manager state: ${t.message}")
            } finally {
                runCatching { authOrNull()?.signOut() }
            }
        }
    }

    private companion object {
        const val TAG = "Auth"
    }
}

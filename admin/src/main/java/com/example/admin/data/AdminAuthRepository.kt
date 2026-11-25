package com.example.admin.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.admin.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminAuthRepository(private val context: Context) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    }

    companion object {
        private const val TAG = "AdminAuthRepository"
        const val BOOTSTRAP_ADMIN_EMAIL = "sujitmehta696@gmail.com"
    }

    val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun signInWithGoogle(activityContext: Context): Result<AdminSession> {
        return try {
            val serverClientId = activityContext.getString(R.string.default_web_client_id)
            val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()
            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )
            val credential = result.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(IllegalStateException("Unsupported credential type returned."))
            }
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authResult = auth.signInWithCredential(
                GoogleAuthProvider.getCredential(googleCredential.idToken, null)
            ).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Google authentication succeeded without user profile."))
            verifyAdminAccess(firebaseUser)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Result.failure(IllegalStateException("Sign-in cancelled."))
        } catch (e: Exception) {
            Log.e(TAG, "Admin sign-in failed", e)
            Result.failure(IllegalStateException(e.localizedMessage ?: "Admin sign-in failed."))
        }
    }

    suspend fun verifyAdminAccess(user: FirebaseUser): Result<AdminSession> {
        return try {
            val email = user.email.orEmpty()
            if (user.isEmailVerified && email.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)) {
                return Result.success(
                    AdminSession(
                        uid = user.uid,
                        email = email,
                        displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "Root Admin",
                        role = "SUPER_ADMIN"
                    )
                )
            }
            val adminDoc = firestore.collection("admins").document(user.uid).get().await()
            if (adminDoc.exists()) {
                val role = adminDoc.getString("role") ?: "CONTENT_ADMIN"
                val name = adminDoc.getString("displayName")?.takeIf { it.isNotBlank() }
                    ?: user.displayName?.takeIf { it.isNotBlank() }
                    ?: "Admin"
                Result.success(
                    AdminSession(
                        uid = user.uid,
                        email = email,
                        displayName = name,
                        role = role
                    )
                )
            } else {
                withContext(NonCancellable) {
                    auth.signOut()
                }
                Result.failure(
                    SecurityException(
                        "Access denied for $email. Sign in with an authorized admin Google account."
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Admin role verification failed", e)
            Result.failure(IllegalStateException("Unable to verify admin privileges: ${e.localizedMessage}"))
        }
    }

    suspend fun signOut() {
        withContext(NonCancellable) {
            auth.signOut()
        }
    }
}

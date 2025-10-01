package mm.zamiec.garpom.auth

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mm.zamiec.garpom.model.AppUser
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ActivityRetainedScoped
class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) {

    val TAG = "AuthRepository"

    val executor: Executor = Executor { it.run() }

    val userExists: Boolean
        get() = firebaseAuth.currentUser != null

    val currentUser: Flow<AppUser>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let {
                        AppUser(it.uid, it.displayName ?: "Unnamed", it.isAnonymous)

                    } ?: AppUser())
                }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }

    fun signInAnonymously() {
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(executor) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInAnonymously:success")
                } else {
                    Log.e(TAG, "signInAnonymously:failure", task.exception)
                }
            }
    }

    init {
        Log.i(TAG, "Initialized")
        if (!userExists) {
            signInAnonymously()
        }
    }

    fun signOut() {
        Log.d(TAG, "Signing out")
        firebaseAuth.signOut()
        signInAnonymously()
    }

    fun startPhoneNumberVerification(
        phoneNumber: String,
        timeout: Long = 60L
    ): Flow<PhoneVerificationStatus> = callbackFlow {
        Log.d(TAG, "launched phone verification flow")
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(PhoneVerificationStatus.VerificationCompleted(credential))
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                trySend(PhoneVerificationStatus.Error(exception.message ?: "Verification failed"))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(PhoneVerificationStatus.CodeSent(verificationId, token))
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(timeout, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        Log.d(TAG, "PhoneAuthProvider called")

        awaitClose { }
        Log.d(TAG, "phone verification flow closed")
    }

    suspend fun getCredentialWithCode(
        verificationId: String,
        code: String
    ): PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId, code)
    }

    suspend fun linkWithCredential(credential: AuthCredential): VerificationResult {
        if (firebaseAuth.currentUser == null) {
            Log.e(TAG, "Link called when no account was set")
            return VerificationResult.Error("Fatal error")
        }
        return try {
            firebaseAuth.currentUser!!.linkWithCredential(credential).await()
            Log.d(TAG, "Linked account successfully")
            VerificationResult.Verified
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.w(TAG, "Malformed or invalid credential")
            VerificationResult.InvalidCredential
        } catch (e: FirebaseAuthUserCollisionException) {
            val msg = "There is already another account associated with these credentials"
            Log.w(TAG, msg)
            // TODO check correctness?
            signInWithCredential(credential)
//            VerificationResult.Error(msg)
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e(TAG, "Anonymous account invalid")
            signInWithCredential(credential)
//            VerificationResult.Error("This account is invalid")
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Attempt to link a provider that is already linked to this account or internal auth error")
            VerificationResult.Error("Internal server error")
        } catch (e: Exception) {
            Log.e(TAG, "Other fatal error: " + e.message)
            VerificationResult.Error("Internal error")
        }
    }

    suspend fun signInWithCredential(credential: AuthCredential): VerificationResult {
        return try {
            firebaseAuth.signInWithCredential(credential).await()
            VerificationResult.Verified
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            VerificationResult.InvalidCredential
        } catch (e: Exception) {
            VerificationResult.Error(e.message ?: "Unknown error")
        }
    }
}

sealed class PhoneVerificationStatus {
    data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneVerificationStatus()
    data class CodeSent(
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken
    ) : PhoneVerificationStatus()
    data class Error(val message: String) : PhoneVerificationStatus()

}

sealed class VerificationResult {
    data object Verified : VerificationResult()

    data object InvalidCredential : VerificationResult()

    data class Error(val message: String) : VerificationResult()
}
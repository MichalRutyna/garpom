package mm.zamiec.garpom.auth

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ActivityRetainedScoped
class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) {

    val TAG = "AuthRepository"

    val executor: Executor = Executor { it.run() }

    init {
        if (currentUser == null) {
            signInAnonymously()
        }
    }

    fun signOut() = {
        firebaseAuth.signOut()
        signInAnonymously()
    }

    val currentUser get() = firebaseAuth.currentUser

    val isAnonymous get() = currentUser!!.isAnonymous

    fun signInAnonymously() {
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(executor) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "signInAnonymously:failure", task.exception)
                }
            }
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
        if (currentUser == null) {
            Log.e(TAG, "Link called when no account was set")
            return VerificationResult.Error("Fatal error")
        }
        return try {
            currentUser!!.linkWithCredential(credential).await()
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

    fun test(){
        val req = UserProfileChangeRequest.Builder()
            .setDisplayName("Test")
            .build()
        currentUser?.updateProfile(req)
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
package mm.zamiec.garpom.auth

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) {

    val executor: Executor = Executor { it.run() }

    val TAG = "AuthRepository"

    fun signOut() = firebaseAuth.signOut()

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

    suspend fun signInWithCredential(credential: AuthCredential): PhoneVerificationResult {
        return try {
            firebaseAuth.signInWithCredential(credential).await()
            PhoneVerificationResult.SignedIn("Sign in successfull")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            PhoneVerificationResult.InvalidCode("Code entered was invalid")
        } catch (e: Exception) {
            PhoneVerificationResult.Error(e.message ?: "Unknown error")
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

sealed class PhoneVerificationResult {
    data class SignedIn(val message: String) : PhoneVerificationResult()

    data class InvalidCode(val message: String) : PhoneVerificationResult()

    data class Error(val message: String) : PhoneVerificationResult()
}
package mm.zamiec.garpom.controller.auth

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import mm.zamiec.garpom.domain.model.AppUser
import mm.zamiec.garpom.domain.usecase.TokenUseCase
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val serverInteractor: TokenUseCase,
    private val appUserRepository: AppUserRepository,
) {

    val TAG = "AuthRepository"

    val executor: Executor = Executor { it.run() }

    val userExists: Boolean
        get() = firebaseAuth.currentUser != null

    val currentUser: StateFlow<AppUser> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser

            if (firebaseUser == null) {
                // User signed out
                trySend(AppUser())
                return@AuthStateListener
            }

            launch {
                if (firebaseUser.isAnonymous) {
                    trySend(
                        AppUser(
                            id = firebaseUser.uid,
                            username = firebaseUser.displayName ?: "Unnamed",
                            isAnonymous = true
                        )
                    )
                } else {
                    appUserRepository
                        .getUserById(firebaseUser.uid)
                        .collect { user ->
                            trySend(user)
                        }
                }
            }
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.stateIn(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        started = SharingStarted.Eagerly,
        initialValue = AppUser()
    )


    fun signInAnonymously() {
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(executor) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInAnonymously:success")
                    serverInteractor.ensureToken()
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
        serverInteractor.ensureToken()
    }

    fun signOut() {
        Log.d(TAG, "Signing out")
        firebaseAuth.signOut()
        signInAnonymously()
    }

    suspend fun changeUsername(username: String): ChangeUsernameResult =
        suspendCancellableCoroutine { cont ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                cont.resume(ChangeUsernameResult.Error("User not logged in"))
            } else {
                val listener = OnCompleteListener<Void?> { result ->
                    if (result.isSuccessful) {
                        Log.d(TAG, "Changed username successful")
                        cont.resume(ChangeUsernameResult.Success)
                    } else {
                        Log.d(TAG, "Changed username failed/canceled")
                        cont.resume(ChangeUsernameResult.Error("Operation failed"))
                    }
                }
                appUserRepository.changeUsername(user.uid, username)
                    .addOnCompleteListener(listener)
                    .addOnFailureListener { e ->
                        cont.resume(ChangeUsernameResult.Error(e.message ?: "Unknown error"))
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
        if (firebaseAuth.currentUser == null) {
            Log.e(TAG, "Link called when no account was set")
            return VerificationResult.Error("Fatal error")
        }
        return try {
            firebaseAuth.currentUser!!.linkWithCredential(credential).await()
            Log.d(TAG, "Linked account successfully")
            serverInteractor.ensureToken()
            VerificationResult.Verified
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.w(TAG, "Malformed or invalid credential")
            VerificationResult.InvalidCredential
        } catch (e: FirebaseAuthUserCollisionException) {
            val msg = "There is already another account associated with these credentials, logging into it"
            Log.w(TAG, msg)
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
        Log.d(TAG, "Signing in with credentials")
        return try {
            firebaseAuth.signInWithCredential(credential).await()
            Log.d(TAG, "Verification successful")
            serverInteractor.ensureToken()
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

sealed class ChangeUsernameResult {
    data object Success: ChangeUsernameResult()
    data object Loading: ChangeUsernameResult()
    data class Error(val message: String) : ChangeUsernameResult()
}
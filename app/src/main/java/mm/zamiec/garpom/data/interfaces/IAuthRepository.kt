package mm.zamiec.garpom.data.interfaces

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import mm.zamiec.garpom.data.auth.ChangeUsernameResult
import mm.zamiec.garpom.data.auth.PhoneVerificationStatus
import mm.zamiec.garpom.data.auth.VerificationResult
import mm.zamiec.garpom.domain.model.AppUser

interface IAuthRepository {

    val userExists: Boolean
    val currentUser: StateFlow<AppUser>

    fun signInAnonymously()
    fun signOut()

    suspend fun changeUsername(username: String): ChangeUsernameResult
    fun startPhoneNumberVerification(phoneNumber: String, timeout: Long = 60L): Flow<PhoneVerificationStatus>
    suspend fun getCredentialWithCode(verificationId: String, code: String): PhoneAuthCredential
    suspend fun linkWithCredential(credential: AuthCredential): VerificationResult
    suspend fun signInWithCredential(credential: AuthCredential): VerificationResult
}
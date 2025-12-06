package mm.zamiec.garpom.mocks

import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import mm.zamiec.garpom.data.auth.ChangeUsernameResult
import mm.zamiec.garpom.data.auth.PhoneVerificationStatus
import mm.zamiec.garpom.data.auth.VerificationResult
import mm.zamiec.garpom.data.interfaces.IAuthRepository
import mm.zamiec.garpom.domain.model.AppUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : IAuthRepository {

    override val userExists: Boolean = true
    override val currentUser = MutableStateFlow(AppUser("userId", "username"))

    override fun signInAnonymously() {}
    override fun signOut() {}
    override suspend fun changeUsername(username: String) = ChangeUsernameResult.Success

    override fun startPhoneNumberVerification(phoneNumber: String, timeout: Long) =
        MutableStateFlow<PhoneVerificationStatus>(PhoneVerificationStatus.CodeSent(
            verificationId = "fakeVerificationId"
        ))

    override suspend fun getCredentialWithCode(verificationId: String, code: String) =
        throw NotImplementedError()

    override suspend fun linkWithCredential(credential: AuthCredential) = VerificationResult.Verified
    override suspend fun signInWithCredential(credential: AuthCredential) = VerificationResult.Verified
}
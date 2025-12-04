package mm.zamiec.garpom.ui.view_models

import android.util.Log
import com.google.firebase.auth.PhoneAuthCredential
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import mm.zamiec.garpom.data.auth.*
import mm.zamiec.garpom.ui.screens.auth.AuthUiState
import mm.zamiec.garpom.ui.screens.auth.AuthViewModel
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var repository: AuthRepository
    private lateinit var vm: AuthViewModel

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mockk()
        vm = AuthViewModel(repository)

        mockkStatic(android.util.Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------
    // startPhoneNumberVerification()
    // -------------------------------------------------------------

    @Test
    fun `startPhoneNumberVerification returns auto verification`() = runTest {
        val credential = mockk<PhoneAuthCredential>()

        every { repository.startPhoneNumberVerification(any()) } returns flow {
            emit(PhoneVerificationStatus.VerificationCompleted(credential))
        }
        coEvery { repository.linkWithCredential(credential) } returns VerificationResult.Verified

        vm.startPhoneNumberVerification("+123")
        advanceUntilIdle()

        assertThat(vm.uiState.value, instanceOf(AuthUiState.Success::class.java))
    }

    @Test
    fun `startPhoneNumberVerification returns Error`() = runTest {
        every { repository.startPhoneNumberVerification(any()) } returns flow {
            emit(PhoneVerificationStatus.Error("failure"))
        }

        vm.startPhoneNumberVerification("+123")
        advanceUntilIdle()

        assertThat(vm.uiState.value, instanceOf(AuthUiState.Error::class.java))
        assertThat((vm.uiState.value as AuthUiState.Error).message, `is`("failure"))
    }

    @Test
    fun `startPhoneNumberVerification catches exception`() = runTest {
        every { repository.startPhoneNumberVerification(any()) } returns flow {
            throw RuntimeException("boom")
        }

        vm.startPhoneNumberVerification("+123")
        advanceUntilIdle()

        assertThat(vm.uiState.value, instanceOf(AuthUiState.Error::class.java))
        assertThat((vm.uiState.value as AuthUiState.Error).message, `is`("boom"))
    }

    // -------------------------------------------------------------
    // verifyCode()
    // -------------------------------------------------------------

    @Test
    fun `verifyCode uses credential and signs in`() = runTest {
        val credential = mockk<PhoneAuthCredential>()
        vm.verificationId.value = "vid123"

        coEvery { repository.getCredentialWithCode("vid123", "0000") } returns credential
        coEvery { repository.linkWithCredential(credential) } returns VerificationResult.Verified

        vm.verifyCode("0000")
        advanceUntilIdle()

        assertThat(vm.uiState.value, instanceOf(AuthUiState.Success::class.java))
    }

    // -------------------------------------------------------------
    // signInWithPhoneCredential()
    // -------------------------------------------------------------

    @Test
    fun `signInWithPhoneCredential returns Success`() = runTest {
        val cred = mockk<PhoneAuthCredential>()
        coEvery { repository.linkWithCredential(cred) } returns VerificationResult.Verified

        vm.signInWithPhoneCredential(cred)
        advanceUntilIdle()

        assertThat(vm.uiState.value, instanceOf(AuthUiState.Success::class.java))
    }

    @Test
    fun `signInWithPhoneCredential returns InvalidCredential`() = runTest {
        val cred = mockk<PhoneAuthCredential>()
        coEvery { repository.linkWithCredential(cred) } returns VerificationResult.InvalidCredential

        vm.signInWithPhoneCredential(cred)
        advanceUntilIdle()

        assertThat(vm.uiState.value, instanceOf(AuthUiState.Error::class.java))
        assertThat((vm.uiState.value as AuthUiState.Error).message, `is`("Invalid code"))
    }

    @Test
    fun `signInWithPhoneCredential returns Error`() = runTest {
        val cred = mockk<PhoneAuthCredential>()
        coEvery { repository.linkWithCredential(cred) } returns VerificationResult.Error("Oops")

        vm.signInWithPhoneCredential(cred)
        advanceUntilIdle()

        assertThat(vm.uiState.value, instanceOf(AuthUiState.Error::class.java))
        assertThat((vm.uiState.value as AuthUiState.Error).message, `is`("Oops"))
    }

    // -------------------------------------------------------------
    // backed()
    // -------------------------------------------------------------

    @Test
    fun `backed resets state and cancels job`() = runTest {
        every { repository.startPhoneNumberVerification(any()) } returns flow {
            delay(Long.MAX_VALUE)
        }

        vm.startPhoneNumberVerification("+123")
        advanceUntilIdle()

        vm.backed()

        assertThat(vm.uiState.value, instanceOf(AuthUiState.Idle::class.java))
        assertThat(vm.job?.isCancelled, `is`(true))
    }
}

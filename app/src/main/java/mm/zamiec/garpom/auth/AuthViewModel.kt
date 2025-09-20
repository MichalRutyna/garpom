package mm.zamiec.garpom.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "AuthViewModel"
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    val verificationId = MutableStateFlow<String?>(null)

    fun startPhoneNumberVerification(phoneNumber: String) {
        Log.d(TAG, "Started verification")
        _uiState.value = AuthUiState.Loading

        repository.startPhoneNumberVerification(phoneNumber)
            .onEach { result ->
                when (result) {
                    is PhoneVerificationStatus.CodeSent -> {
                        _uiState.value = AuthUiState.CodeSent
                        verificationId.value = result.verificationId
                    }
                    is PhoneVerificationStatus.VerificationCompleted -> {
                        // Auto verification completed
                        signInWithPhoneCredential(result.credential)
                    }
                    is PhoneVerificationStatus.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            }
            .catch { e ->
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
            .launchIn(viewModelScope)
    }

    fun verifyCode(code: String) {
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val credential = repository.getCredentialWithCode(verificationId.value!!, code) //TODO: handle null, verification not started
            signInWithPhoneCredential(credential)
        }
    }

    fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val result = repository.signInWithCredential(credential)
            when (result) {
                is PhoneVerificationResult.SignedIn -> {
                    _uiState.value = AuthUiState.Success
                }
                is PhoneVerificationResult.InvalidCode -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is PhoneVerificationResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun test(): String {
        return "Hello world"
    }
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object CodeSent : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
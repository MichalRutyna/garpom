package mm.zamiec.garpom.ui.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mm.zamiec.garpom.auth.AuthRepository
import mm.zamiec.garpom.auth.PhoneVerificationStatus
import mm.zamiec.garpom.auth.VerificationResult
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "AuthViewModel"
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    val verificationId = MutableStateFlow<String?>(null)

    var job: Job? = null

    fun startPhoneNumberVerification(phoneNumber: String) {
        Log.d(TAG, "Started verification")
        _uiState.value = AuthUiState.Loading

        job = repository.startPhoneNumberVerification(phoneNumber)
            .onEach { result ->
                Log.d(TAG, "result: "+ result)
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
        Log.d(TAG, "Sign in started")
        viewModelScope.launch {
//            val result = repository.signInWithCredential(credential)
            val result = repository.linkWithCredential(credential)
            when (result) {
                is VerificationResult.Verified -> {
                    _uiState.value = AuthUiState.Success
                }
                is VerificationResult.InvalidCredential -> {
                    _uiState.value = AuthUiState.Error("Invalid code")
                }
                is VerificationResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun backed() {
        _uiState.value = AuthUiState.Idle
        job?.cancel()
    }
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object CodeSent : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
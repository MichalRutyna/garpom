package mm.zamiec.garpom.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.controller.auth.ChangeUsernameResult
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "ProfileViewModel"

    val uiState = repository.currentUser.map { user ->
        ProfileScreenState(
            userId = user.id,
            username = user.username,
            isAnonymous = user.isAnonymous
        )
    }

    fun logOut() {
        repository.signOut()
    }

    suspend fun changeUsername(username: String): ChangeUsernameScreenState {
        return when (repository.changeUsername(username)) {
                is ChangeUsernameResult.Success -> ChangeUsernameScreenState.Success
                is ChangeUsernameResult.Error -> ChangeUsernameScreenState.Error("Username change failed")
                else -> ChangeUsernameScreenState.Loading
            }
    }
}
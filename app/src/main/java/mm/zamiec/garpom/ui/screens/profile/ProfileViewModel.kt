package mm.zamiec.garpom.ui.screens.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.data.auth.ChangeUsernameResult
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
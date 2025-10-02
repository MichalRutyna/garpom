package mm.zamiec.garpom.ui.screens.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import mm.zamiec.garpom.controller.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "ProfileViewModel"

    val uiState = repository.currentUser.map { user ->
        ProfileState(
            userId = user.id,
            username = user.username,
            isAnonymous = user.isAnonymous
        )
    }

    fun logOut() {
        repository.signOut()
    }
}
package mm.zamiec.garpom.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import mm.zamiec.garpom.auth.AuthRepository
import mm.zamiec.garpom.ui.screens.configure.ConfigureState
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "ProfileViewModel"

    val uiState = repository.currentUser.map { user ->
        ProfileState(
            username = user.username,
            isAnonymous = user.isAnonymous
        )
    }

    fun logOut() {
        repository.signOut()
    }
}
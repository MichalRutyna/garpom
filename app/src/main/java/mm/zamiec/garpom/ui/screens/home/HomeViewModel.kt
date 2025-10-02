package mm.zamiec.garpom.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import mm.zamiec.garpom.controller.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "HomeViewModel"

    val uiState = repository.currentUser.map { user ->
        HomeState(
            isAnonymous = user.isAnonymous,
            username = user.username,
            stations = listOf(
                StationSummary(stationId="1", name="South station"),
                StationSummary(stationId="2", name="North station", hasError = true),
                StationSummary(stationId="3", name="East station", hasNotification = true),
                StationSummary(stationId="4", name="West station", hasError = true, hasNotification = true),
            )
        )
    }

}
package mm.zamiec.garpom.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.domain.managers.AlarmOccurrencesListManager
import mm.zamiec.garpom.domain.managers.StationSummaryManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
    stationSummaryManager: StationSummaryManager,
    alarmOccurrencesListManager: AlarmOccurrencesListManager
) : ViewModel() {

    val uiState: Flow<HomeUiState> =
        combine(
            authRepository.currentUser,
            stationSummaryManager.stationsSummaryForUser(),
            alarmOccurrencesListManager.recentAlarmOccurrences(),
        ) { user, stations, alarms ->
            HomeUiState(
                isAnonymous = user.isAnonymous,
                username = user.username,
                stations = stations,
                recentAlarmOccurrences = alarms,
            )
        }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
package mm.zamiec.garpom.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.ui.state.HomeUiState
import mm.zamiec.garpom.domain.managers.AlarmOccurrencesListMenager
import mm.zamiec.garpom.domain.managers.StationSummaryManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
    stationSummaryManager: StationSummaryManager,
    alarmOccurrencesListMenager: AlarmOccurrencesListMenager
) : ViewModel() {

    val uiState: Flow<HomeUiState> =
        combine(
            authRepository.currentUser,
            stationSummaryManager.stationsSummaryForUser(),
            alarmOccurrencesListMenager.recentAlarmOccurrences(),
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
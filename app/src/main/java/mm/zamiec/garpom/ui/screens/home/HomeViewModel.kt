package mm.zamiec.garpom.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.domain.model.state.HomeState
import mm.zamiec.garpom.domain.usecase.AlarmOccurrencesListUseCase
import mm.zamiec.garpom.domain.usecase.StationSummaryUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
    stationSummaryUseCase: StationSummaryUseCase,
    alarmOccurrencesListUseCase: AlarmOccurrencesListUseCase
) : ViewModel() {

    val uiState: Flow<HomeState> =
        combine(
            authRepository.currentUser,
            stationSummaryUseCase.stationsSummaryForUser(),
            alarmOccurrencesListUseCase.recentAlarmOccurrences(),
        ) { user, stations, alarms ->
            HomeState(
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
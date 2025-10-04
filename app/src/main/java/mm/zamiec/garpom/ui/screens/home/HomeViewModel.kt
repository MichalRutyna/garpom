package mm.zamiec.garpom.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.domain.model.state.HomeState
import mm.zamiec.garpom.domain.model.state.RecentAlarmOccurrence
import mm.zamiec.garpom.domain.model.state.StationSummary
import mm.zamiec.garpom.domain.usecase.AlarmOccurrencesListUseCase
import mm.zamiec.garpom.domain.usecase.StationSummaryUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val stationSummaryUseCase: StationSummaryUseCase,
    private val alarmOccurrencesListUseCase: AlarmOccurrencesListUseCase
) : ViewModel() {

    private val TAG = "HomeViewModel"

    @OptIn(ExperimentalCoroutinesApi::class)
    val userStations: Flow<List<StationSummary>> =
        authRepository.currentUser.flatMapLatest { user ->
            stationSummaryUseCase.stationSummary(user.id)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentAlarmsOccurrences: Flow<List<RecentAlarmOccurrence>> =
        authRepository.currentUser.flatMapLatest { user ->
            alarmOccurrencesListUseCase.recentAlarmOccurrences(user.id)
        }


    val uiState: Flow<HomeState> =
        combine(
            authRepository.currentUser,
            userStations,
            recentAlarmsOccurrences,
        ) { user, stations, alarms ->
            HomeState(
                isAnonymous = user.isAnonymous,
                username = user.username,
                stations = stations,
                recentAlarmOccurrences = alarms,
            )
        }

}
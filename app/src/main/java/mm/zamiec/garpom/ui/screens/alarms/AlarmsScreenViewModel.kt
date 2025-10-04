package mm.zamiec.garpom.ui.screens.alarms

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.domain.model.state.AlarmOccurrence
import mm.zamiec.garpom.domain.model.state.AlarmsScreenState
import mm.zamiec.garpom.domain.model.state.RecentAlarmOccurrence
import mm.zamiec.garpom.domain.model.state.StationAlarms
import mm.zamiec.garpom.domain.usecase.AlarmOccurrencesListUseCase
import mm.zamiec.garpom.domain.usecase.AlarmsListUseCase
import javax.inject.Inject

@HiltViewModel
class AlarmsScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val alarmOccurrencesListUseCase: AlarmOccurrencesListUseCase,
    private val alarmsListUseCase: AlarmsListUseCase,
) : ViewModel() {

    private val TAG = "AlarmsScreenViewModel"

    @OptIn(ExperimentalCoroutinesApi::class)
    val userAlarms: Flow<List<StationAlarms>> =
        authRepository.currentUser.flatMapLatest { user ->
            alarmsListUseCase.alarmList(user.id)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentAlarmsOccurrences: Flow<List<RecentAlarmOccurrence>> =
        authRepository.currentUser.flatMapLatest { user ->
            alarmOccurrencesListUseCase.recentAlarmOccurrences(user.id)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allAlarmsOccurrences: Flow<List<AlarmOccurrence>> =
        authRepository.currentUser.flatMapLatest { user ->
            alarmOccurrencesListUseCase.allAlarmOccurrences(user.id)
        }


    val uiState: Flow<AlarmsScreenState> =
        combine(
            userAlarms,
            recentAlarmsOccurrences,
            allAlarmsOccurrences
        ) { alarms, recentOcc, allOcc ->
            Log.d("test", recentOcc.toString())
            AlarmsScreenState(
                stationAlarmsList = alarms,
                recentOcc,
                allOcc
            )
        }
}
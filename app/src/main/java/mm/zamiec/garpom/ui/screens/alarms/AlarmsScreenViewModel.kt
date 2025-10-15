package mm.zamiec.garpom.ui.screens.alarms

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mm.zamiec.garpom.domain.model.state.AlarmsScreenState
import mm.zamiec.garpom.domain.usecase.AlarmOccurrencesListUseCase
import mm.zamiec.garpom.domain.usecase.AlarmsListUseCase
import javax.inject.Inject

@HiltViewModel
 class AlarmsScreenViewModel @Inject constructor(
    alarmOccurrencesListUseCase: AlarmOccurrencesListUseCase,
    alarmsListUseCase: AlarmsListUseCase,
) : ViewModel() {

    val uiState: Flow<AlarmsScreenState> =
        combine(
            alarmsListUseCase.alarmList(),
            alarmOccurrencesListUseCase.recentAlarmOccurrences(),
            alarmOccurrencesListUseCase.allAlarmOccurrences()
        ) { alarms, recentOcc, allOcc ->
            AlarmsScreenState(
                stationAlarmsList = alarms,
                recentOcc,
                allOcc
            )
        }

    companion object {
        private const val TAG = "AlarmsScreenViewModel"
    }
}
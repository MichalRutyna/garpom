package mm.zamiec.garpom.ui.screens.alarms

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mm.zamiec.garpom.ui.state.AlarmsUiState
import mm.zamiec.garpom.domain.managers.AlarmOccurrencesListMenager
import mm.zamiec.garpom.domain.managers.AlarmsListManager
import javax.inject.Inject

@HiltViewModel
 class AlarmsScreenViewModel @Inject constructor(
    alarmOccurrencesListMenager: AlarmOccurrencesListMenager,
    alarmsListManager: AlarmsListManager,
) : ViewModel() {

    val uiState: Flow<AlarmsUiState> =
        combine(
            alarmsListManager.alarmList(),
            alarmOccurrencesListMenager.recentAlarmOccurrences(),
            alarmOccurrencesListMenager.allAlarmOccurrences()
        ) { alarms, recentOcc, allOcc ->
            AlarmsUiState(
                stationAlarmsList = alarms,
                recentOcc,
                allOcc
            )
        }

    companion object {
        private const val TAG = "AlarmsScreenViewModel"
    }
}
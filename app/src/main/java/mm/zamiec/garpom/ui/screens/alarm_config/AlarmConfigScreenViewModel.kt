package mm.zamiec.garpom.ui.screens.alarm_config

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mm.zamiec.garpom.domain.managers.AlarmConfigManager
import mm.zamiec.garpom.domain.model.Parameter
import java.util.Calendar
import java.util.Date

@HiltViewModel(assistedFactory = AlarmConfigScreenViewModel.Factory::class)
class AlarmConfigScreenViewModel @AssistedInject constructor(
    private val alarmConfigManager: AlarmConfigManager,
    @Assisted private val alarmId: String,
) : ViewModel() {

    private val TAG = "AlarmConfigScreenViewModel"

    private val _uiState = MutableStateFlow<AlarmConfigUiState>(AlarmConfigUiState.Loading)
    val uiState: StateFlow<AlarmConfigUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = AlarmConfigUiState.Loading
        viewModelScope.launch {
            _uiState.value = alarmConfigManager.alarmDetails(alarmId)
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun saveStates(
        alarmEnabled: Boolean,
        alarmName: String,
        alarmDescription: String,
        alarmStartTimeState: TimePickerState,
        alarmEndTimeState: TimePickerState,
        stationUsingThisAlarm: SnapshotStateList<StationChoice>,
        sliderPositionsState:  Map<String, MutableState<ClosedFloatingPointRange<Float>>>,
        ) {

        if (uiState.value !is AlarmConfigUiState.ConfigData)
            return
        val stateSnapshot = uiState.value as AlarmConfigUiState.ConfigData

        val rangesSnapshot = stateSnapshot.cards.toMutableList()
        sliderPositionsState.forEach { (title, position) ->
            val i = rangesSnapshot.indexOfFirst { it.title == title }
            rangesSnapshot[i] =
                ParameterCardFactory.create(
                    Parameter.entries.find { it.title == title } ?: return,
                    position.value.start.toDouble(),
                    position.value.endInclusive.toDouble()
                )
        }

        val uiState = AlarmConfigUiState.ConfigData(
            alarmId = stateSnapshot.alarmId,
            createAlarm = stateSnapshot.createAlarm,
            alarmEnabled = alarmEnabled,
            alarmName = alarmName,
            alarmDescription = alarmDescription,
            userStations = stationUsingThisAlarm,
            alarmStart =
                    Calendar.Builder()
                        .setTimeOfDay(alarmStartTimeState.hour, alarmStartTimeState.minute, 0)
                        .build(),
            alarmEnd =
                Calendar.Builder()
                    .setTimeOfDay(alarmEndTimeState.hour, alarmEndTimeState.minute, 0)
                    .build(),
            cards = rangesSnapshot
        )
        viewModelScope.launch {
            alarmConfigManager.saveUiState(uiState)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(alarmId: String): AlarmConfigScreenViewModel
    }
}
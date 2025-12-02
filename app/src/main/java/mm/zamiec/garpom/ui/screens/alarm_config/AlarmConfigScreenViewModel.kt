package mm.zamiec.garpom.ui.screens.alarm_config

import android.nfc.Tag
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.painterResource
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

    val editState = MutableStateFlow(AlarmConfigEditState())

    init {
        _uiState.value = AlarmConfigUiState.Loading
        viewModelScope.launch {
            val state = if (alarmId == "") {
                alarmConfigManager.getNewAlarmState()
            } else {
                alarmConfigManager.alarmDetails(alarmId)
            }
            _uiState.value = state
            resetFromUiState()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun resetFromUiState() {
        val state = _uiState.value as AlarmConfigUiState.ConfigData
        editState.value = AlarmConfigEditState().apply {
            alarmEnabled.value = state.alarmEnabled
            alarmName.value = state.alarmName
            alarmDescription.value = state.alarmDescription
            alarmStart = TimePickerState(
                initialHour = state.alarmStart.get(Calendar.HOUR_OF_DAY),
                initialMinute = state.alarmStart.get(Calendar.MINUTE),
                is24Hour = true,
            )
            alarmEnd = TimePickerState(
                initialHour = state.alarmEnd.get(Calendar.HOUR_OF_DAY),
                initialMinute = state.alarmEnd.get(Calendar.MINUTE),
                is24Hour = true,
            )
            stations.clear()
            stations.addAll(state.userStations)
            sliderPositions.clear()
            sliderPositions.putAll(cardsToMap(state))
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun saveStates() {
        if (uiState.value !is AlarmConfigUiState.ConfigData)
            return
        val stateSnapshot = uiState.value as AlarmConfigUiState.ConfigData

        val rangesSnapshot = stateSnapshot.cards.toMutableList()
        editState.value.sliderPositions.forEach { (title, position) ->
            val i = rangesSnapshot.indexOfFirst { it.title == title }
            rangesSnapshot[i] =
                ParameterCardFactory.create(
                    Parameter.entries.find { it.title == title } ?: return,
                    position.start.toDouble(),
                    position.endInclusive.toDouble()
                )
        }

        val uiState = AlarmConfigUiState.ConfigData(
            alarmId = stateSnapshot.alarmId,
            createAlarm = stateSnapshot.createAlarm,
            alarmEnabled = editState.value.alarmEnabled.value,
            alarmName = editState.value.alarmName.value,
            alarmDescription = editState.value.alarmDescription.value,
            userStations = editState.value.stations,
            alarmStart =
                    Calendar.Builder()
                        .setTimeOfDay(editState.value.alarmStart.hour, editState.value.alarmStart.minute, 0)
                        .build(),
            alarmEnd =
                Calendar.Builder()
                    .setTimeOfDay(editState.value.alarmEnd.hour, editState.value.alarmEnd.minute, 0)
                    .build(),
            cards = rangesSnapshot
        )
        viewModelScope.launch {
            alarmConfigManager.saveUiState(uiState)
        }.invokeOnCompletion {
            _uiState.value = AlarmConfigUiState.GoBack
        }
    }

    fun resetSliders() {
        editState.value.sliderPositions.clear()
        editState.value.sliderPositions.putAll(cardsToMap(uiState.value as AlarmConfigUiState.ConfigData))
    }


    @AssistedFactory
    interface Factory {
        fun create(alarmId: String): AlarmConfigScreenViewModel
    }
}
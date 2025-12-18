package mm.zamiec.garpom.ui.screens.alarm_config

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.sql.Time
import java.time.Instant
import java.util.Calendar
import kotlin.collections.component1
import kotlin.collections.component2

interface AlarmConfigUiState {
    class ConfigData(
        val alarmId: String = "",
        val createAlarm: Boolean = true,
        val alarmEnabled: Boolean = true,
        val alarmName: String = "Unnamed alarm",
        val alarmDescription: String = "Custom description",
        val userStations: List<StationChoice> = emptyList(),

        val alarmStart: Calendar = Calendar.Builder().setTimeOfDay(0, 0, 0).build(),
        val alarmEnd: Calendar = Calendar.Builder().setTimeOfDay(24, 0, 0).build(),

        val cards: List<ParameterRangeCard> =
            getInitialRangesMutableMap().map { (parameter, values) ->
                ParameterCardFactory.create(
                    parameter,
                    values.first,
                    values.second
                )
            }
    ) : AlarmConfigUiState

    data class Error(val message: String) : AlarmConfigUiState

    object Loading : AlarmConfigUiState

    object GoBack : AlarmConfigUiState
}

data class StationChoice (
    val stationId: String,
    val stationName: String,
    var hasThisAlarm: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
// Non-saved, ui-specific data
class AlarmConfigEditState {
    var alarmEnabled = mutableStateOf(false)

    var alarmName = mutableStateOf("")
    var alarmDescription = mutableStateOf("")
    var alarmStart = TimePickerState(0, 0, true)
    var alarmEnd = TimePickerState(0, 0, true)

    var stations = mutableStateListOf<StationChoice>()
    var sliderPositions = mutableStateMapOf<String, ClosedFloatingPointRange<Float>>()
}
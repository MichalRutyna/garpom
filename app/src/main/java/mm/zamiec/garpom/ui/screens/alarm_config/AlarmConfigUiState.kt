package mm.zamiec.garpom.ui.screens.alarm_config

import java.time.Instant
import java.util.Calendar
import kotlin.collections.component1
import kotlin.collections.component2

open class AlarmConfigUiState() {
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
    ) : AlarmConfigUiState()

    data class Error(val message: String) : AlarmConfigUiState()

    object Loading : AlarmConfigUiState()
}
data class StationChoice (
    val stationId: String,
    val stationName: String,
    var hasThisAlarm: Boolean,
) {
}


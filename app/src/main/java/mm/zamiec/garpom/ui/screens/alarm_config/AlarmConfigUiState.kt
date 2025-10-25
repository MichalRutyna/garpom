package mm.zamiec.garpom.ui.screens.alarm_config

import java.util.Calendar
import java.util.Date

open class AlarmConfigUiState() {
    class ConfigData(
        val alarmId: String = "",
        val createAlarm: Boolean,
        val alarmEnabled: Boolean,
        val alarmName: String,
        val alarmDescription: String,
        val userStations: List<StationChoice>,

        val alarmStart: Calendar,
        val alarmEnd: Calendar,

        val cards: List<ParameterRangeCard>
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


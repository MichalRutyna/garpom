package mm.zamiec.garpom.ui.screens.alarm_config

import java.util.Date

open class AlarmConfigUiState() {
    class ConfigData(
        val alarmId: String?,
        val createAlarm: Boolean,
        val alarmActive: Boolean,
        val alarmName: String,
        val userStations: List<StationChoice>,

        val alarmStart: Date,
        val alarmEnd: Date,

        val cards: List<ParameterRangeCard>
    ) : AlarmConfigUiState()

    data class Error(val message: String) : AlarmConfigUiState()

    object Loading : AlarmConfigUiState()
}
class StationChoice (
    val stationId: String,
    val stationName: String,
    val hasThisAlarm: Boolean,
)
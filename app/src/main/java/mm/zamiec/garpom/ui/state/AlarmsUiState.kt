package mm.zamiec.garpom.ui.state

data class AlarmsUiState (
    val stationAlarmsList: List<StationAlarmsItemUiState> = emptyList(),
    val recentAlarmOccurrencesList: List<RecentAlarmOccurrenceItemUiState> = emptyList(),
    val allAlarmOccurrencesList: List<AlarmOccurrenceItemUiState> = emptyList(),
)

data class StationAlarmsItemUiState (
    val stationName: String = "",
    val stationId: String = "",
    val alarmList: List<AlarmSummaryItemUiState>,
)

data class AlarmSummaryItemUiState (
    val alarmId: String,
    val name: String,
    val recentlyWentOff: Boolean,
)

data class AlarmOccurrenceItemUiState(
    val alarmName: String = "",
    val measurementId: String = ""
)

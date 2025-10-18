package mm.zamiec.garpom.ui.screens.alarms

import mm.zamiec.garpom.ui.screens.home.RecentAlarmOccurrenceItemUiState

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

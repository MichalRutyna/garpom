package mm.zamiec.garpom.domain.model.state

import mm.zamiec.garpom.domain.usecase.AlarmOccurrencesListUseCase

data class AlarmsScreenState (
    val stationAlarmsList: List<StationAlarms> = emptyList(),
    val recentAlarmOccurrencesList: List<RecentAlarmOccurrence> = emptyList(),
    val allAlarmOccurrencesList: List<AlarmOccurrence> = emptyList(),
)

data class StationAlarms (
    val stationName: String,
    val alarmList: List<AlarmSummary>,
)

data class AlarmSummary (
    val alarmId: String,
    val name: String,
    val recentlyWentOff: Boolean,
)

data class AlarmOccurrence(
    val alarmName: String = "",
    val measurementId: String = ""
)

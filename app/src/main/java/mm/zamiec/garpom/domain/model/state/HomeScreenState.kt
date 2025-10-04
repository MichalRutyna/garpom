package mm.zamiec.garpom.domain.model.state

data class StationSummary(
    val stationId: String = "",
    val name: String = "",
    val hasNotification: Boolean = false,
    val hasError: Boolean = false,

)

data class RecentAlarmOccurrence(
    val alarmName: String = "",
    val measurementId: String = ""
)

data class HomeState(
    val isAnonymous: Boolean = true,
    val username: String = "",
    val stations: List<StationSummary> = listOf(),
    val recentAlarmOccurrences: List<RecentAlarmOccurrence> = listOf(),
)
package mm.zamiec.garpom.domain.model

data class StationSummary(
    val stationId: String = "",
    val name: String = "",
    val hasNotification: Boolean = false,
    val hasError: Boolean = false,

)

data class RecentAlarm(
    val alarmName: String = "",
    val measurementId: String = ""
)

data class HomeState(
    val isAnonymous: Boolean = true,
    val username: String = "",
    val stations: List<StationSummary> = listOf(),
    val recentAlarms: List<RecentAlarm> = listOf(),
)
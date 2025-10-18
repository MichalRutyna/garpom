package mm.zamiec.garpom.ui.screens.home

data class StationSummaryItemUiState(
    val stationId: String = "",
    val name: String = "",
    val hasNotification: Boolean = false,
    val hasError: Boolean = false,

)

data class RecentAlarmOccurrenceItemUiState(
    val alarmName: String = "",
    val measurementId: String = ""
)

data class HomeUiState(
    val isAnonymous: Boolean = true,
    val username: String = "",
    val stations: List<StationSummaryItemUiState> = listOf(),
    val recentAlarmOccurrences: List<RecentAlarmOccurrenceItemUiState> = listOf(),
)
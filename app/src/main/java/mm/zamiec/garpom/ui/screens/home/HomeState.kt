package mm.zamiec.garpom.ui.screens.home

data class StationSummary(
    val stationId: String = "",
    val name: String = "",
    val hasNotification: Boolean = false,
    val hasError: Boolean = false,

)

data class HomeState(
    val isAnonymous: Boolean = true,
    val username: String = "",
    val stations: List<StationSummary> = listOf()
)
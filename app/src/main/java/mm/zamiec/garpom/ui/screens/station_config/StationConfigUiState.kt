package mm.zamiec.garpom.ui.screens.station_config

sealed class StationConfigUiState {
    data object WifiList: StationConfigUiState()

    data object Connecting : StationConfigUiState()

    data class ServiceDiscoveryData(
        val serviceData: MutableList<HashMap<String, String>> = mutableListOf(),
        val characteristicsData: MutableList<ArrayList<HashMap<String, String>>> = mutableListOf(),
    ): StationConfigUiState()

    data class PasswordInput(
        val selection: WifiSelection
    ): StationConfigUiState()

    data class Error(
        val message: String
    ): StationConfigUiState()
}

data class WifiSelection (
    val name: String,
    val ssid: String,
    val locked: Boolean,
    val connected: Boolean,
)
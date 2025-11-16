package mm.zamiec.garpom.ui.screens.configure

open class ConfigureScreenUiState(
) {
    object Idle: ConfigureScreenUiState()
    object PermissionConfirmed: ConfigureScreenUiState()
    object PermissionDialog: ConfigureScreenUiState()
    object BluetoothRejected: ConfigureScreenUiState()
    object DeviceIncompatible: ConfigureScreenUiState()

    class ServiceData (
        val serviceData: MutableList<HashMap<String, String>>,
        val characteristicsData: MutableList<ArrayList<HashMap<String, String>>>
    ): ConfigureScreenUiState()
}
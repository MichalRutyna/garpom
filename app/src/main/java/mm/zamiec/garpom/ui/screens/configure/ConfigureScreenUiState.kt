package mm.zamiec.garpom.ui.screens.configure

import android.bluetooth.BluetoothGatt

sealed class ScreenState {
    object Initial : ScreenState()
    object Scanning : ScreenState()
    object ScanResults : ScreenState()
    data class PairingError(val message: String) : ScreenState()

    data class TempStationScreen(val station: BluetoothGatt) : ScreenState()
    data class ServiceDiscoveryData(
        val serviceData: MutableList<HashMap<String, String>>,
        val characteristicsData: MutableList<ArrayList<HashMap<String, String>>>
    ): ScreenState()
}

sealed class DialogState {
    object PermissionExplanationNeeded : DialogState()
    object PermissionsDenied : DialogState()
    object DeviceIncompatible : DialogState()
}

data class ConfigureUiState(
    val screenState: ScreenState = ScreenState.Initial,
    val dialog: DialogState? = null
)

class StationScanResult(
    val address: String,
    val name: String
)
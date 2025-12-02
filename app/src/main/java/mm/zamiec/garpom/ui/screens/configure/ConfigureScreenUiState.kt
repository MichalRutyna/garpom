package mm.zamiec.garpom.ui.screens.configure

sealed class ScreenState {
    object Initial : ScreenState()
    object Scanning : ScreenState()
    object ScanResults : ScreenState()
    data class PairingError(val message: String) : ScreenState()
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
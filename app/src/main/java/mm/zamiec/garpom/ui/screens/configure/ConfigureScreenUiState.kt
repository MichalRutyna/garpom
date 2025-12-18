package mm.zamiec.garpom.ui.screens.configure

sealed class ScreenState {
    data object Initial : ScreenState()
    data object Scanning : ScreenState()
    data object ScanResults : ScreenState()
    data class PairingError(val message: String) : ScreenState()
}

sealed class DialogState {
    data object PermissionExplanationNeeded : DialogState()
    data object PermissionsDenied : DialogState()
    data object DeviceIncompatible : DialogState()
}

data class ConfigureUiState(
    val screenState: ScreenState = ScreenState.Initial,
    val dialog: DialogState? = null
)

data class StationScanResult(
    val address: String,
    val name: String
)
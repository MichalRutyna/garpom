package mm.zamiec.garpom.ui.state

open class ConfigureScreenUiState(
) {
    object Idle: ConfigureScreenUiState()
    object PermissionConfirmed: ConfigureScreenUiState()
    object PermissionDialog: ConfigureScreenUiState()
    object BluetoothRejected: ConfigureScreenUiState()
    object DeviceIncompatible: ConfigureScreenUiState()
}
package mm.zamiec.garpom.ui.screens.configure

open class ConfigureScreenUiState(
) {
    object Idle: ConfigureScreenUiState()
    object PermissionConfirmed: ConfigureScreenUiState()
    object PermissionDialog: ConfigureScreenUiState()
    object BluetoothRejected: ConfigureScreenUiState()
    object DeviceIncompatible: ConfigureScreenUiState()
}
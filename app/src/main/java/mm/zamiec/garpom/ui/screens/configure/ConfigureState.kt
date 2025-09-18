package mm.zamiec.garpom.ui.screens.configure

public open class ConfigureState(
) {
    object Idle: ConfigureState()
    object PermissionConfirmed: ConfigureState()
    object PermissionDialog: ConfigureState()
    object BluetoothRejected: ConfigureState()
    object DeviceIncompatible: ConfigureState()
}
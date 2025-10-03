package mm.zamiec.garpom.domain.model.state

open class ConfigureScreenState(
) {
    object Idle: ConfigureScreenState()
    object PermissionConfirmed: ConfigureScreenState()
    object PermissionDialog: ConfigureScreenState()
    object BluetoothRejected: ConfigureScreenState()
    object DeviceIncompatible: ConfigureScreenState()
}
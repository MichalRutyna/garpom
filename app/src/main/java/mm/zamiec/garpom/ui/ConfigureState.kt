package mm.zamiec.garpom.ui

import android.graphics.Bitmap

public open class ConfigureState(
) {
    object Idle: ConfigureState()
    object PermissionConfirmed: ConfigureState()
    object PermissionDialog: ConfigureState()
    object BluetoothRejected: ConfigureState()
    object DeviceIncompatible: ConfigureState()
}
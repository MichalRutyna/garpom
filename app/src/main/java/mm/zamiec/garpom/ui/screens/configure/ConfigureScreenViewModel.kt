package mm.zamiec.garpom.ui.screens.configure

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.invoke
import kotlin.text.clear

@HiltViewModel
class ConfigureScreenViewModel @Inject constructor (
    @ApplicationContext private val context: Context,
    private val btManager: BluetoothManager
) : ViewModel() {
    companion object {
        const val TAG = "ConfigureScreenViewModel"
    }

    private val _uiState = MutableStateFlow(ConfigureUiState())
    val uiState: StateFlow<ConfigureUiState> = _uiState.asStateFlow()

    var scanResults = mutableStateListOf<StationScanResult>()
        private set

    fun hasBtPermissions(): Boolean {
        val scan = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

        val connect = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
        val location = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


        return scan && connect && location
    }

    @SuppressLint("MissingPermission") // permission check in this function
    fun initialConfiguration(activity: Activity) {
//        if (bluetoothAdapter == null) {
//            _uiState.value = _uiState.value.copy(dialog = DialogState.DeviceIncompatible)
//            return
//        }

        if (!hasBtPermissions()) {
            val shouldShowRationaleScan = ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.BLUETOOTH_SCAN
            )
            val shouldShowRationaleConnect = ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.BLUETOOTH_CONNECT
            )

            if (shouldShowRationaleScan || shouldShowRationaleConnect) {
                _uiState.value = _uiState.value.copy(dialog = DialogState.PermissionExplanationNeeded)
            } else {
                // first-time request
                _uiState.value = _uiState.value.copy(dialog = null)
                _requestPermissionsCallback?.invoke()
            }
            return
        }
        initiatePairing()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun initiatePairing() {
        try {
            _uiState.value = _uiState.value.copy(screenState = ScreenState.Scanning)
            scanResults.clear()
            seenAddresses.clear()
            btManager.initiatePairing(
                resultCallback = { result ->
                    showScanResult(result)
                },
                onFinishedCallback = {
                    if (_uiState.value.screenState != ScreenState.ScanResults)
                        _uiState.value = _uiState.value.copy(screenState = ScreenState.ScanResults)
                }
            )
        } catch (e: DeviceIncompatibleException) {
            _uiState.value = _uiState.value.copy(dialog = DialogState.DeviceIncompatible)
        } catch (e: BluetoothDisabled) {
            Log.d(TAG, "Requesting bluetooth enable")
            _bluetoothEnableCallback?.invoke()
        }
    }

    val seenAddresses = mutableSetOf<String>()

    @SuppressLint("MissingPermission")
    fun showScanResult(result: ScanResult) {
        val address = result.device.address
        Log.d(BluetoothManager.Companion.TAG, "Found result: "+address)
        if (seenAddresses.add(address)) {
            scanResults.add(
                StationScanResult(
                    result.device.address,
                    result.device.name ?: ""
                )
            )
        }
        if(scanResults.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(screenState = ScreenState.ScanResults)
        }
    }

    // callback set by the Composable to trigger permission launcher
    private var _requestPermissionsCallback: (() -> Unit)? = null
    fun setRequestPermissionsCallback(callback: () -> Unit) {
        _requestPermissionsCallback = callback
    }

    private var _bluetoothEnableCallback: (() -> Unit)? = null
    fun setBluetoothEnableCallback(callback: () -> Unit) {
        _bluetoothEnableCallback = callback
    }
    fun onExplanationAccepted() {
        _uiState.value = _uiState.value.copy(dialog = null)
        _requestPermissionsCallback?.invoke()
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun onPermissionsGranted() {
        _uiState.value = _uiState.value.copy(dialog = null)
        initiatePairing()
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun onBluetoothEnabled() {
        initiatePairing()
    }
    fun onPermissionsDenied() {
        _uiState.value = _uiState.value.copy(dialog = DialogState.PermissionsDenied)
    }
    fun clearDialog() {
        _uiState.value = _uiState.value.copy(dialog = null)
    }
}
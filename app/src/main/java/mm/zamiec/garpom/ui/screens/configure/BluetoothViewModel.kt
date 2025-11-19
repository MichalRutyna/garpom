package mm.zamiec.garpom.ui.screens.configure

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mm.zamiec.garpom.data.bluetooth.scanAsFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor (
    @ApplicationContext private val context: Context
) : ViewModel() {
    val TAG = "Bluetooth"

    private val _uiState = MutableStateFlow(ConfigureUiState())
    val uiState: StateFlow<ConfigureUiState> = _uiState.asStateFlow()

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

    private val bluetoothManager: BluetoothManager? = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private val SCAN_TIMEOUT = 10_000L
    private var scanJob: Job? = null
    private var _scanResultsDevices = mutableListOf<ScanResult>()
    var scanResults = mutableStateListOf<StationScanResult>()
        private set


    @SuppressLint("MissingPermission") // permission check in this function
    fun initialConfiguration(activity: Activity) {
        if (bluetoothAdapter == null) {
            _uiState.value = _uiState.value.copy(dialog = DialogState.DeviceIncompatible)
            return
        }

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
    fun initiatePairing() {
        if (bluetoothAdapter == null) {
            _uiState.value = _uiState.value.copy(dialog = DialogState.DeviceIncompatible)
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Log.d(TAG, "Requesting bluetooth enable")
            _bluetoothEnableCallback?.invoke()
            return
        }
        scanBluetooth()
    }

    fun scanBluetooth() {
        Log.d(TAG, "Connecting")
        _uiState.value = _uiState.value.copy(screenState = ScreenState.Scanning)
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        if (scanJob != null) return

        scanResults.clear()
        seenAddresses.clear()

        scanJob = viewModelScope.launch {
            withTimeoutOrNull(SCAN_TIMEOUT) {
                bluetoothLeScanner?.scanAsFlow()?.collect { result ->
                    showScanResult(result)
                }
            }
            stopScan()
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        if (_uiState.value.screenState != ScreenState.ScanResults)
            _uiState.value = _uiState.value.copy(screenState = ScreenState.ScanResults)

    }

    override fun onCleared() {
        stopScan()
        super.onCleared()
    }

    val seenAddresses = mutableSetOf<String>()

    @SuppressLint("MissingPermission")
    fun showScanResult(result: ScanResult) {
        val address = result.device.address
        Log.d(TAG, "Found result: "+address)
        if (seenAddresses.add(address)) {
            _scanResultsDevices.add(result)
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToResultByAddress(address: String) {
        stopScan()
        val result = _scanResultsDevices.find { it.device.address == address }

        if (result == null) {
            _uiState.value = _uiState.value.copy(screenState = ScreenState.PairingError("Invalid address selected"))
            return
        }
        result.device!!.connectGatt(context, false, bluetoothGattCallback)
    }
    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (gatt == null)
                    return
                Log.d(TAG, "Connected to Gatt server")
                _uiState.value =
                    _uiState.value.copy(screenState = ScreenState.TempStationScreen(gatt))
//                gatt?.discoverServices()
//                gatt?.writeCharacteristic()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Gatt disconnected")
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                gatt?.services?.find { service ->
////                    service.uuid.
//                }
                val gattServices = gatt?.services
                Log.d(TAG, "Gatt services discovered: "+gattServices)
                if (gattServices == null) return

                var uuid: String?
                val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
                val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
                    mutableListOf()

                gattServices.forEach { gattService ->
                    val currentServiceData = HashMap<String, String>()
                    uuid = gattService.uuid.toString()
                    currentServiceData["type"] = gattService.type.toString()
                    currentServiceData["uuid"] = uuid
                    gattServiceData += currentServiceData

                    val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
                    val gattCharacteristics = gattService.characteristics
                    val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()

                    // Loops through available Characteristics.
                    gattCharacteristics.forEach { gattCharacteristic ->
                        charas += gattCharacteristic
                        val currentCharaData: HashMap<String, String> = hashMapOf()
                        uuid = gattCharacteristic.uuid.toString()
                        currentCharaData["name"] = gattCharacteristic.descriptors.toString()
                        currentCharaData["uuid"] = uuid
                        gattCharacteristicGroupData += currentCharaData
                    }
                    gattCharacteristicData += gattCharacteristicGroupData
                }

                Log.d(TAG, "Services: " + gattServiceData)
                Log.d(TAG, "Characteristics: " + gattCharacteristicData)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read: " + gatt.readCharacteristic(characteristic))
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun discoverServices(gatt: BluetoothGatt) {
        gatt.discoverServices()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun testLight(gatt: BluetoothGatt, red: Int, green: Int, blue: Int) {
        val value = "#%02x%02x%02x".format(red, green, blue)
        Log.d(TAG, "Sending '${value}'")
        gatt.writeCharacteristic(
            gatt.services.find { service ->
                service.uuid.equals(UUID.fromString("12345678-1234-1234-1234-1234567890ab"))
            }?.characteristics?.find { characteristic ->
                characteristic.uuid.equals(UUID.fromString("87654321-4321-4321-4321-ba0987654321"))
            } ?: return,
            value.toByteArray(),
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        )
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
package mm.zamiec.garpom.ui.screens.configure

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import mm.zamiec.garpom.data.bluetooth.scanAsFlow
import mm.zamiec.garpom.di.ApplicationScope
import mm.zamiec.garpom.ui.screens.station_config.StationConfigUiState
import mm.zamiec.garpom.ui.screens.station_config.WifiSelection
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BluetoothManager @Inject constructor (
    @param:ApplicationScope private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    companion object {
        const val TAG = "BluetoothManager"

        val LIGHT_CHARACTERISTIC_UUID = "87654321-4321-4321-4321-ba0987654321"
        val WIFI_NETWORKS_CHARACTERISTICS_UUID = UUID.fromString("87654321-4321-4321-4321-ba0987654321")
        val WIFI_CONNECT_CHARACTERISTICS_UUID = ""
        val USERID_CONNECT_CHARACTERISTICS_UUID = ""
    }

    private val bluetoothManager: BluetoothManager? = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    private val SCAN_TIMEOUT = 10_000L
    private var scanJob: Job? = null
    private var _scanResultsDevices = mutableListOf<ScanResult>()


    private var characteristicsReferenceMap = HashMap<String, BluetoothGattCharacteristic>()
    private var _gatt: BluetoothGatt? = null
    private val _isConnected = MutableStateFlow<Boolean?>(null)
    val isConnected: StateFlow<Boolean?> = _isConnected

    private val _wifiList = MutableStateFlow<List<WifiSelection>>(emptyList())
    val wifiList: StateFlow<List<WifiSelection>> = _wifiList

    private val _discoveryData = MutableStateFlow<StationConfigUiState.ServiceDiscoveryData>(
        StationConfigUiState.ServiceDiscoveryData()
    )
    val discoveryData: StateFlow<StationConfigUiState.ServiceDiscoveryData> = _discoveryData

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun initiatePairing(resultCallback: (ScanResult) -> Unit, onFinishedCallback: () -> Unit) {
        if (bluetoothAdapter == null) {
            throw DeviceIncompatibleException()
        }
        if (!bluetoothAdapter.isEnabled) {
            throw BluetoothDisabled()
        }
        scanBluetooth(resultCallback, onFinishedCallback)
    }

    fun scanBluetooth(resultCallback: (ScanResult) -> Unit, onFinishedCallback: () -> Unit) {
        Log.d(TAG, "Connecting")
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        if (scanJob != null) return

        scanJob = scope.launch {
            withTimeoutOrNull(SCAN_TIMEOUT) {
                bluetoothLeScanner?.scanAsFlow()?.collect { result ->
                    _scanResultsDevices.add(result) // for later connections
                    resultCallback(result)
                }
            }
            onFinishedCallback()
            stopScan()
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null

    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToResultByAddress(address: String) {
        stopScan()
        val result = _scanResultsDevices.find { it.device.address == address }

        if (result == null) {
            Log.e(TAG, "Invalid address selected")
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
                _gatt = gatt
                _isConnected.update { true }
                gatt.discoverServices()

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Gatt disconnected")
                _gatt = null
                _isConnected.update { false }
            }
        }
        @SuppressLint("MissingPermission")
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
                        currentCharaData["name"] = gattCharacteristic.descriptors.toList().toString()
                        currentCharaData["uuid"] = uuid
                        characteristicsReferenceMap[uuid] = gattCharacteristic

                        gattCharacteristicGroupData += currentCharaData

                        if (gattCharacteristic.uuid == WIFI_NETWORKS_CHARACTERISTICS_UUID) {
                            gatt.readCharacteristic(gattCharacteristic)
                        }
                    }
                    gattCharacteristicData += gattCharacteristicGroupData
                }

                Log.d(TAG, "Services: " + gattServiceData)
                Log.d(TAG, "Characteristics: " + gattCharacteristicData)
                scope.launch {
                    _discoveryData.emit(StationConfigUiState.ServiceDiscoveryData(
                        gattServiceData, gattCharacteristicData
                    ))
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read: " + gatt.readCharacteristic(characteristic))
            }
            if (characteristic.uuid == WIFI_NETWORKS_CHARACTERISTICS_UUID) {
                scope.launch {
                    _wifiList.emit(convertToWifiSelections(value))
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            if (characteristic.uuid == WIFI_NETWORKS_CHARACTERISTICS_UUID) {
                scope.launch {
                    _wifiList.emit(convertToWifiSelections(value))
                }
            }
        }
    }

    fun convertToWifiSelections(value: ByteArray): List<WifiSelection> {
        val list: List<WifiSelection> = emptyList()
        return list
    }

    fun convertToConnectOrder(ssid: String, password: String?): ByteArray {
        return byteArrayOf()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun discoverServices() {
        _gatt?.discoverServices()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun testLight(red: Int, green: Int, blue: Int) {
        if (_gatt == null)
            return
        val gatt = _gatt!!
        val value = "#%02x%02x%02x".format(red, green, blue)
        Log.d(TAG, "Sending '${value}'")
        gatt.writeCharacteristic(
            characteristicsReferenceMap["87654321-4321-4321-4321-ba0987654321"] ?: return,
            value.toByteArray(),
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendConnectionOrder(ssid: String, password: String?) {
        val value = convertToConnectOrder(ssid, password)
        _gatt?.writeCharacteristic(characteristicsReferenceMap[WIFI_CONNECT_CHARACTERISTICS_UUID] ?: return, value,
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
    }
}

class DeviceIncompatibleException: Throwable()
class BluetoothDisabled: Throwable()
class CantConnect: Throwable()
package mm.zamiec.garpom.ui.screens.configure

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.MacAddress
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mm.zamiec.garpom.ui.screens.configure.ConfigureScreenUiState
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor (
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConfigureScreenUiState>(ConfigureScreenUiState.Idle)
    val uiState: StateFlow<ConfigureScreenUiState> = _uiState.asStateFlow()

    private val _bluetoothPermissionGranted = MutableStateFlow(false)

    val TAG = "Bluetooth"
    val executor: Executor = Executor { it.run() }

    val deviceManager: CompanionDeviceManager by lazy {
        ContextCompat.getSystemService(
            context,
            CompanionDeviceManager::class.java
        ) as CompanionDeviceManager
    }

    fun updatePermissionStatus(granted: Boolean) {
        _bluetoothPermissionGranted.value = granted
        if (granted and (_uiState.value == ConfigureScreenUiState.BluetoothRejected)) {
            alertPermissionConfirmed()

        }
    }

    fun pair(activity: Activity, btPermissionLauncher: ActivityResultLauncher<String>, btEnableLauncher: ActivityResultLauncher<Intent>, pairingLauncher: ActivityResultLauncher<IntentSenderRequest>) {

        val bluetoothManager: BluetoothManager? =
            ContextCompat.getSystemService(context, BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
        if (bluetoothAdapter == null) {
            Log.w(TAG,"Bluetooth is not supported on this device")
            _uiState.value = ConfigureScreenUiState.DeviceIncompatible
            return
        }

        if (!_bluetoothPermissionGranted.value) {
            btPermissionLauncher.launch(
                Manifest.permission.BLUETOOTH_CONNECT)
            // connectBluetooth() from callback
            return
        }

        if (bluetoothAdapter.isEnabled) {
            Log.d(TAG, "Adapter enabled, connecting")
            connectBluetooth(pairingLauncher)
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            btEnableLauncher.launch(enableBtIntent)
            // connectBluetooth() from callback
        }


    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Gatt connected, discovering")
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Gatt disconnected")
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
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
                _uiState.value = ConfigureScreenUiState.ServiceData(gattServiceData, gattCharacteristicData)
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

    fun connectBluetooth(pairingLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        Log.d(TAG, "Connecting")
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
//            .setNamePattern(Pattern.compile("My device"))
//            .addServiceUuid(ParcelUuid(UUID(0x123abcL, -1L)), null)
            .build()
        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(false)
            .build()

        deviceManager.associate(
            pairingRequest,
            executor,
            object : CompanionDeviceManager.Callback() {
                // Called when a device is found.
                override fun onAssociationPending(intentSender: IntentSender) {
                    pairingLauncher.launch(
                        IntentSenderRequest.Builder(intentSender)
                            .build()
                    )
                }
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    var associationId: Int = associationInfo.id
                    var macAddress: MacAddress? = associationInfo.deviceMacAddress
                    Log.d(TAG, "Mac Address: $macAddress")

                    Log.d(TAG, "Associated:" + associationInfo.associatedDevice)
                    Log.d(TAG, "Ble device:" + associationInfo.associatedDevice?.bleDevice)
                    Log.d(TAG, "Device:" + associationInfo.associatedDevice?.bleDevice?.device)
                    associationInfo.associatedDevice?.bleDevice?.device?.connectGatt(context, false, bluetoothGattCallback)

                }
                override fun onFailure(errorMessage: CharSequence?) {
                    Log.e(TAG, "Pairing error")
                }
            }
        )
    }

    fun alertPermissionConfirmed() {
        _uiState.value = ConfigureScreenUiState.PermissionConfirmed
    }

    fun alertPairingLaunched() {
        _uiState.value = ConfigureScreenUiState.Idle
        // TODO special state
    }

    fun alertPermissionRejected() {
        _uiState.value = ConfigureScreenUiState.BluetoothRejected
    }

    fun clearDialog() {
        _uiState.value = ConfigureScreenUiState.Idle
    }
}
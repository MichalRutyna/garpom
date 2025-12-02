package mm.zamiec.garpom.ui.screens.station_config

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mm.zamiec.garpom.ui.screens.configure.BluetoothManager

@SuppressLint("MissingPermission")
@HiltViewModel(assistedFactory = StationConfigViewModel.Factory::class)
class StationConfigViewModel @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val address: String,
    private val btManager: BluetoothManager
) : ViewModel() {

    companion object {
        private const val TAG = "StationConfigViewModel"
    }

    private val _uiState = MutableStateFlow<StationConfigUiState>(StationConfigUiState.Connecting)
    val uiState: StateFlow<StationConfigUiState> = _uiState

    val wifiListState: StateFlow<List<WifiSelection>> =
        btManager.wifiList
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    val serviceData: StateFlow<StationConfigUiState.ServiceDiscoveryData> =
        btManager.discoveryData
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = StationConfigUiState.ServiceDiscoveryData()
            )

    val isConnected: StateFlow<Boolean?> =
        btManager.isConnected.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    private val _characteristicSwitch = MutableStateFlow(false)
    val characteristicSwitch: StateFlow<Boolean> = _characteristicSwitch
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

    init {
        viewModelScope.launch {
            if (!hasBtPermissions()) {
                _uiState.update {
                    StationConfigUiState.Error("No permissions")
                }
                return@launch
            }

            runCatching {
                btManager.connectToResultByAddress(address)
            }.onFailure {
                _uiState.update { s ->
                    StationConfigUiState.Error(it.message ?: "BLE error")
                }
                return@launch
            }
        }
        isConnected
            .filterNotNull()
            .onEach { connected ->
                _uiState.update {
                    if (connected) StationConfigUiState.WifiList
                    else StationConfigUiState.Error("Couldn't connect")
                }
            }.launchIn(viewModelScope)
    }

    fun networkChosen(selection: WifiSelection) {
        _uiState.update {
            StationConfigUiState.PasswordInput(selection)
        }
    }

    fun onPasswordDialogDismissed() {
        _uiState.update {
            StationConfigUiState.WifiList
        }
    }

    fun onPasswordEntered(selection: WifiSelection, password: String) {
        btManager.sendConnectionOrder(selection.ssid, password)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun testLight(red: Int, green: Int, blue: Int) {
        btManager.testLight(red, green, blue)
    }

    fun onCharacteristicSwitched(on: Boolean) {
        _characteristicSwitch.value = on
        if (on) {
            _uiState.update {
                StationConfigUiState.ServiceDiscoveryData()
            }
        } else {
            _uiState.update {
                StationConfigUiState.WifiList
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(address: String): StationConfigViewModel
    }
}
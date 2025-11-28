package mm.zamiec.garpom.ui.screens.station_config

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mm.zamiec.garpom.ui.screens.configure.BluetoothManager

@HiltViewModel(assistedFactory = StationConfigViewModel.Factory::class)
class StationConfigViewModel @AssistedInject constructor(
    @Assisted private val address: String,
    private val btManager: BluetoothManager
) : ViewModel() {

    private val TAG = "StationConfigViewModel"

    private val _uiState = MutableStateFlow<StationConfigUiState>(StationConfigUiState(address))
    val uiState: StateFlow<StationConfigUiState> = _uiState

    init {
    }

    @AssistedFactory
    interface Factory {
        fun create(address: String): StationConfigViewModel
    }
}
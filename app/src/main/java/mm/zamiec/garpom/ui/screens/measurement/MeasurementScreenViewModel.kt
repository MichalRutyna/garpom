package mm.zamiec.garpom.ui.screens.measurement


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.domain.managers.MeasurementDetailsManager

@HiltViewModel(assistedFactory = MeasurementScreenViewModel.Factory::class)
class MeasurementScreenViewModel @AssistedInject constructor(
    private val repository: AuthRepository,
    private val measurementDetailsManager: MeasurementDetailsManager,
    @Assisted private val measurementId: String,
) : ViewModel() {

    private val TAG = "MeasurementScreenViewModel"

    private val _uiState = MutableStateFlow<MeasurementScreenState>(MeasurementScreenState.Loading)
    val uiState: StateFlow<MeasurementScreenState> = _uiState.asStateFlow()

    init {
        loadMeasurement()
    }

    private fun loadMeasurement() {
        _uiState.value = MeasurementScreenState.Loading
        viewModelScope.launch {
            val snapshot = measurementDetailsManager.measurementDetailsSnapshot(measurementId)
            _uiState.value = snapshot
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): MeasurementScreenViewModel
    }
}
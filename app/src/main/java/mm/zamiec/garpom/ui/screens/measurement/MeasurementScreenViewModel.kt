package mm.zamiec.garpom.ui.screens.measurement


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.domain.usecase.MeasurementDetailsUseCase

@HiltViewModel(assistedFactory = MeasurementScreenViewModel.Factory::class)
class MeasurementScreenViewModel @AssistedInject constructor(
    private val repository: AuthRepository,
    private val measurementDetailsUseCase: MeasurementDetailsUseCase,
    @Assisted private val measurementId: String,
) : ViewModel() {

    private val TAG = "MeasurementScreenViewModel"

    private val _uiState = MutableStateFlow<MeasurementScreenState>(MeasurementScreenState.Loading)
    val uiState: StateFlow<MeasurementScreenState> = _uiState.asStateFlow()

    init {
        loadMeasurement()
    }

    private fun loadMeasurement() {
        viewModelScope.launch {
            _uiState.value = MeasurementScreenState.Loading
            val snapshot = measurementDetailsUseCase.measurementDetailsSnapshot(measurementId)
            _uiState.value = snapshot
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): MeasurementScreenViewModel
    }
}
package mm.zamiec.garpom.ui.screens.measurement


import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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

    val uiState: Flow<MeasurementScreenState> =
        measurementDetailsUseCase.measurementDetails(measurementId)

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): MeasurementScreenViewModel
    }
}
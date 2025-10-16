package mm.zamiec.garpom.ui.screens.station

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.ui.state.StationScreenUiState
import mm.zamiec.garpom.domain.managers.StationDetailsManager

@HiltViewModel(assistedFactory = StationViewModel.Factory::class)
class StationViewModel @AssistedInject constructor(
    private val repository: AuthRepository,
    private val stationDetailsManager: StationDetailsManager,
    @Assisted private val stationId: String,
) : ViewModel() {

    private val TAG = "StationViewModel"

    val uiState: Flow<StationScreenUiState> =
        stationDetailsManager.stationDetails(stationId)

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): StationViewModel
    }
}
package mm.zamiec.garpom.ui.screens.station

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.domain.model.state.StationScreenState
import mm.zamiec.garpom.domain.usecase.StationDetailsUseCase

@HiltViewModel(assistedFactory = StationViewModel.Factory::class)
class StationViewModel @AssistedInject constructor(
    private val repository: AuthRepository,
    private val stationDetailsUseCase: StationDetailsUseCase,
    @Assisted private val stationId: String,
) : ViewModel() {

    private val TAG = "StationViewModel"

    val uiState: Flow<StationScreenState> =
        stationDetailsUseCase.stationDetails(stationId)

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): StationViewModel
    }
}
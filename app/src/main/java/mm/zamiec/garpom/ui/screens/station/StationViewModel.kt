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

@HiltViewModel(assistedFactory = StationViewModel.Factory::class)
class StationViewModel @AssistedInject constructor(
    private val repository: AuthRepository,
    @Assisted private val stationId: String,
) : ViewModel() {

    private val TAG = "StationViewModel"

    val uiState: Flow<StationState>
        get() = callbackFlow {
            val listener = Firebase.firestore
                .collection("stations")
                .document(stationId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val document = snapshot?.data
                    val station = document?.let { doc ->
                        StationState(
                            name = doc["name"] as String
                        )
                    } ?: StationState()

                    trySend(station)
                }
            awaitClose { listener.remove() }
        }

    @AssistedFactory
    interface Factory {
        fun create(stationId: String): StationViewModel
    }
}
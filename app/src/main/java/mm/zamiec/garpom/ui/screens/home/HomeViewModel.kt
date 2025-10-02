package mm.zamiec.garpom.ui.screens.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.model.AppUser
import javax.inject.Inject
import kotlin.text.get

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "HomeViewModel"

    @OptIn(ExperimentalCoroutinesApi::class)
    val userStations: Flow<List<StationSummary>> =
        // for flattening a nested flow
        repository.currentUser.flatMapLatest { user ->
            callbackFlow {
                val listener = Firebase.firestore.collection("stations")
                    .whereEqualTo("owner_id", user.id)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val stations = snapshot?.documents?.map { doc ->
                            StationSummary(
                                stationId = doc.id,
                                name = doc.getString("name") ?: ""
                            )
                        }.orEmpty()

                        trySend(stations)
                    }

                awaitClose { listener.remove() }
            }
        }

    val uiState: Flow<HomeState> =
        combine(
            repository.currentUser,
            userStations
        ) { user, stations ->
            HomeState(
                isAnonymous = user.isAnonymous,
                username = user.username,
                stations = stations
            )
        }

}
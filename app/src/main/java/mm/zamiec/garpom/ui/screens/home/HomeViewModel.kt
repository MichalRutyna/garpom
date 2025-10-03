package mm.zamiec.garpom.ui.screens.home

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.domain.model.HomeState
import mm.zamiec.garpom.domain.model.RecentAlarm
import mm.zamiec.garpom.domain.model.StationSummary
import mm.zamiec.garpom.domain.usecase.RecentAlarmsUseCase
import mm.zamiec.garpom.domain.usecase.StationSummaryUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val stationSummaryUseCase: StationSummaryUseCase,
    private val recentAlarmsUseCase: RecentAlarmsUseCase,
) : ViewModel() {

    private val TAG = "HomeViewModel"

    @OptIn(ExperimentalCoroutinesApi::class)
    val userStations: Flow<List<StationSummary>> =
        authRepository.currentUser.flatMapLatest { user ->
            stationSummaryUseCase.stationSummary(user.id)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentAlarms: Flow<List<RecentAlarm>> =
        authRepository.currentUser.flatMapLatest { user ->
            recentAlarmsUseCase.recentAlarms(user.id)
        }


    val uiState: Flow<HomeState> =
        combine(
            authRepository.currentUser,
            userStations,
            recentAlarms,
        ) { user, stations, alarms ->
            HomeState(
                isAnonymous = user.isAnonymous,
                username = user.username,
                stations = stations,
                recentAlarms = alarms,
            )
        }

}
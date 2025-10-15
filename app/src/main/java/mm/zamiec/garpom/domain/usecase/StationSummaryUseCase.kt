package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.controller.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.state.StationSummary
import mm.zamiec.garpom.domain.module.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationSummaryUseCase @Inject constructor(
    @param:ApplicationScope private val scope: CoroutineScope,
    authRepository: AuthRepository,

    private val repository: StationRepository
) {
    fun stationsSummaryForUser(): Flow<List<StationSummary>> = _stationsSummaryForUserCache

    init {
        authRepository.currentUser
            .map { it.id }
            .distinctUntilChanged()
            .onEach { userId ->
                observeStationsSummaryForUser(userId)
            }
            .launchIn(scope)
    }

    private val _stationsSummaryForUserCache = MutableStateFlow<List<StationSummary>>(emptyList())
    private var _stationsSummaryForUserJob: Job? = null


    private fun observeStationsSummaryForUser(userId: String) {
        _stationsSummaryForUserJob?.cancel()
        _stationsSummaryForUserJob = repository.getStationsByOwner(userId)
            .map { stations ->
                stations.map { station ->
                    StationSummary(
                        station.id,
                        station.name,
                    )
                }
            }
            .onEach { _stationsSummaryForUserCache.value = it }
            .launchIn(scope)
    }
}
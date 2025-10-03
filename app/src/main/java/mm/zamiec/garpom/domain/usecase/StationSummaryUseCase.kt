package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import mm.zamiec.garpom.controller.firebase.FirestoreRepository
import mm.zamiec.garpom.domain.model.RecentAlarm
import mm.zamiec.garpom.domain.model.StationSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationSummaryUseCase @Inject constructor(private val repository: FirestoreRepository) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun stationSummary(userId: String): Flow<List<StationSummary>> =
        repository.getStationsForUser(userId)
            .map { stations ->
                stations.map { station ->
                    StationSummary(
                        station.id,
                        station.name,
                    )
                }
            }
}
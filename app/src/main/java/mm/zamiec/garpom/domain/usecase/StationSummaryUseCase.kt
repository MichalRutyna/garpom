package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mm.zamiec.garpom.controller.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.state.StationSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationSummaryUseCase @Inject constructor(private val repository: StationRepository) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun stationSummary(userId: String): Flow<List<StationSummary>> =
        repository.getStationsByOwner(userId)
            .map { stations ->
                stations.map { station ->
                    StationSummary(
                        station.id,
                        station.name,
                    )
                }.orEmpty()
            }
}
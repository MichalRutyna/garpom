package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import mm.zamiec.garpom.controller.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.state.StationScreenState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationDetailsUseCase @Inject constructor(private val repository: StationRepository) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stationDetails(stationId: String): Flow<StationScreenState> =
        repository.getStationById(stationId).mapNotNull { station ->
            station?.let {
                StationScreenState(
                    station.name
                )
            } ?: StationScreenState(name = "error 404")
        }
}
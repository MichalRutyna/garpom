package mm.zamiec.garpom.domain.usecase


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import mm.zamiec.garpom.controller.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.domain.model.state.StationScreenState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeasurementDetailsUseCase @Inject constructor(private val repository: MeasurementRepository) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun measurementDetails(measurementId: String): Flow<MeasurementScreenState> =
        repository.getMeasurementById(measurementId).mapNotNull { measurement ->
            measurement?.let {
                MeasurementScreenState(
                    measurement.id
                )
            } ?: MeasurementScreenState( "error 404")
        }
}
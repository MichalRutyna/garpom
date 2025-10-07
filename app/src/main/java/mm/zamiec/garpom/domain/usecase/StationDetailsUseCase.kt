package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import mm.zamiec.garpom.controller.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmRepository
import mm.zamiec.garpom.controller.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.controller.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.state.MeasurementSummary
import mm.zamiec.garpom.domain.model.state.Notification
import mm.zamiec.garpom.domain.model.state.StationScreenState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationDetailsUseCase @Inject constructor(
    private val stationRepository: StationRepository,
    private val measurementRepository: MeasurementRepository,
    private val alarmOccurrenceRepository: AlarmOccurrenceRepository,
    private val alarmRepository: AlarmRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stationDetails(stationId: String): Flow<StationScreenState> =
        stationRepository.getStationById(stationId)
            .flatMapLatest { station ->
            if (station == null)
                return@flatMapLatest flowOf(
                    StationScreenState.Error("Station not found!"))
            val measurements =
                measurementRepository.getMeasurementsByStation(stationId)
                    .mapLatest { measurements ->
                        measurements.map { measurement ->
                            MeasurementSummary(
                                measurement.date,
                                measurement.id
                            )
                        }
                    }
            val alarmNotifications =
                alarmOccurrenceRepository.getAlarmOccurrencesForStation(stationId)
                    .flatMapLatest { occurrences ->
                        if (occurrences.isEmpty()) {
                            return@flatMapLatest flowOf(emptyList())
                        }
                        val occurrencesFlows = occurrences.map { occurrence ->
                            combine (
                                measurementRepository.getMeasurementById(occurrence.measurementId),
                                alarmRepository.getAlarmById(occurrence.alarmId)
                            ) { measurement, alarm ->
                                if (measurement != null && alarm != null) {
                                    Notification.AlarmNotification(
                                        occurrence.measurementId,
                                        measurement.date,
                                        alarm.name
                                    )
                                }
                                else null
                            }

                        }
                        combine(occurrencesFlows) { it.filterNotNull() }
                    }
            combine(
                measurements,
                alarmNotifications
            ) { measurements, alarmNotifications ->
                StationScreenState.StationData(
                    station.name,
                    notifications = alarmNotifications,
                    measurementList = measurements
                )
            }
        }
}
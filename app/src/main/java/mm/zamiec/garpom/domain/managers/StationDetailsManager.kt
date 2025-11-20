package mm.zamiec.garpom.domain.managers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import mm.zamiec.garpom.data.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmRepository
import mm.zamiec.garpom.data.dataRepositories.MeasurementRepository
import mm.zamiec.garpom.data.dataRepositories.StationRepository
import mm.zamiec.garpom.ui.screens.station.MeasurementSummaryItemUiState
import mm.zamiec.garpom.ui.screens.station.NotificationItemUiState
import mm.zamiec.garpom.ui.screens.station.StationScreenUiState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationDetailsManager @Inject constructor(
    private val stationRepository: StationRepository,
    private val measurementRepository: MeasurementRepository,
    private val alarmOccurrenceRepository: AlarmOccurrenceRepository,
    private val alarmRepository: AlarmRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun stationDetails(stationId: String): Flow<StationScreenUiState> =
        stationRepository.getStationById(stationId)
            .flatMapLatest { station ->
            if (station == null)
                return@flatMapLatest flowOf(
                    StationScreenUiState.Error("Station not found!"))
            val measurements =
                measurementRepository.getMeasurementsByStation(stationId)
                    .mapLatest { measurements ->
                        measurements.map { measurement ->
                            MeasurementSummaryItemUiState(
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
                                    NotificationItemUiState.AlarmNotification(
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
                StationScreenUiState.StationData(
                    station.name,
                    notifications = alarmNotifications,
                    measurementList = measurements,

                )
            }
        }
}
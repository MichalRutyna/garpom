package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import mm.zamiec.garpom.controller.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmRepository
import mm.zamiec.garpom.controller.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.state.AlarmSummary
import mm.zamiec.garpom.domain.model.state.StationAlarms
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AlarmsListUseCase @Inject constructor(
    val stationRepository: StationRepository,
    val alarmRepository: AlarmRepository,
    val alarmOccurrenceRepository: AlarmOccurrenceRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun alarmList(ownerId: String): Flow<List<StationAlarms>> =
        stationRepository.getStationsByOwner(ownerId)
            .flatMapLatest { stations ->

                val stationAlarmsFlows = stations.map { station ->
                    alarmRepository.getAlarmsByStation(station.id)
                        .flatMapLatest { alarms ->

                            val alarmSummaries = alarms.map { alarm ->
                                alarmOccurrenceRepository
                                    .getAlarmOccurrencesByAlarm(alarm.id)
                                    .mapLatest { occurrences ->

                                        val recentlyWentOff = occurrences.any { occurrence ->
                                            occurrence.date.toDate().toInstant().isAfter(Instant.now()
                                                .minus(7, ChronoUnit.DAYS))
                                        }
                                        AlarmSummary(
                                            alarmId = alarm.id,
                                            name = alarm.name,
                                            recentlyWentOff = recentlyWentOff
                                        )
                                    }
                            }
                            if (alarmSummaries.isEmpty()) {
                                flowOf(StationAlarms(station.name, station.id,emptyList()))
                            } else {
                                combine(alarmSummaries) { StationAlarms(station.name, station.id, it.toList()) }
                            }
                    }
                }
                if (stationAlarmsFlows.isEmpty())
                    flowOf(emptyList())
                else
                    combine(stationAlarmsFlows) { it.toList() }
            }

}
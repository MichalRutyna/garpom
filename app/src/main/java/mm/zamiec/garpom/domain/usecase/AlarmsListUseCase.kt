package mm.zamiec.garpom.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import mm.zamiec.garpom.controller.auth.AuthRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.controller.dataRepositories.AlarmRepository
import mm.zamiec.garpom.controller.dataRepositories.StationRepository
import mm.zamiec.garpom.domain.model.state.AlarmSummary
import mm.zamiec.garpom.domain.model.state.StationAlarms
import mm.zamiec.garpom.domain.module.ApplicationScope
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AlarmsListUseCase @Inject constructor(
    @param:ApplicationScope private val scope: CoroutineScope,
    authRepository: AuthRepository,

    private val stationRepository: StationRepository,
    private val alarmRepository: AlarmRepository,
    private val alarmOccurrenceRepository: AlarmOccurrenceRepository,
) {
    fun alarmList(): StateFlow<List<StationAlarms>> = _userAlarmsCache

    init {
        authRepository.currentUser
            .map { it.id }
            .distinctUntilChanged()
            .onEach { userId ->
                observeUserAlarms(userId)
            }
            .launchIn(scope)
    }


    private val _userAlarmsCache = MutableStateFlow<List<StationAlarms>>(emptyList())
    private var _userAlarmsJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeUserAlarms(ownerId: String) {
        _userAlarmsJob?.cancel()
        _userAlarmsJob = stationRepository
            .getStationsByOwner(ownerId)
            .flatMapLatest { stations ->
                val stationAlarmsFlows = stations.map { station ->
                    alarmRepository.getAlarmsByStation(station.id)
                        .flatMapLatest { alarms ->
                            val alarmSummaries = alarms.map { alarm ->
                                alarmOccurrenceRepository.getAlarmOccurrencesByAlarm(alarm.id)
                                    .mapLatest { occurrences ->
                                        val recentlyWentOff = occurrences.any {
                                            it.date.toDate().toInstant()
                                                .isAfter(Instant.now().minus(7, ChronoUnit.DAYS))
                                        }
                                        AlarmSummary(alarm.id, alarm.name, recentlyWentOff)
                                    }
                            }
                            if (alarmSummaries.isEmpty()) flowOf(StationAlarms(station.name, station.id, emptyList()))
                            else combine(alarmSummaries) { StationAlarms(station.name, station.id, it.toList()) }
                        }
                }
                if (stationAlarmsFlows.isEmpty()) flowOf(emptyList())
                else combine(stationAlarmsFlows) { it.toList() }
            }
            .onEach { _userAlarmsCache.value = it }
            .launchIn(scope)
    }
}

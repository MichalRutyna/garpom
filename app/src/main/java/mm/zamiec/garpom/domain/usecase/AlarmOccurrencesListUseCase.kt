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
import mm.zamiec.garpom.domain.model.state.AlarmOccurrence
import mm.zamiec.garpom.domain.model.state.RecentAlarmOccurrence
import mm.zamiec.garpom.domain.module.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AlarmOccurrencesListUseCase @Inject constructor (
    @param:ApplicationScope private val scope: CoroutineScope,
    authRepository: AuthRepository,

    private val alarmOccurrenceRepository: AlarmOccurrenceRepository,
    private val alarmRepository: AlarmRepository,
) {

    fun allAlarmOccurrences(): StateFlow<List<AlarmOccurrence>> = _allAlarmOccurrencesCache
    fun recentAlarmOccurrences(): StateFlow<List<RecentAlarmOccurrence>> = _recentAlarmOccurrencesCache

    init {
        authRepository.currentUser
            .map { it.id }
            .distinctUntilChanged()
            .onEach { userId ->
                observeAllAlarmOccurrences(userId)
                observeRecentAlarmOccurrences(userId)
            }
            .launchIn(scope)
    }


    private val _allAlarmOccurrencesCache = MutableStateFlow<List<AlarmOccurrence>>(emptyList())
    private var allAlarmOccurrencesJob: Job? = null

    private val _recentAlarmOccurrencesCache = MutableStateFlow<List<RecentAlarmOccurrence>>(emptyList())
    private var recentAlarmOccurrencesJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeAllAlarmOccurrences(userId: String) {
        allAlarmOccurrencesJob?.cancel()
        allAlarmOccurrencesJob = alarmOccurrenceRepository
            .getAlarmOccurrencesForUser(userId)
            .flatMapLatest { occurrences ->
                val flows = occurrences.map { occurrence ->
                    alarmRepository.getAlarmById(occurrence.alarmId).mapLatest { alarm ->
                        if (alarm == null)
                            return@mapLatest AlarmOccurrence("", "") //TODO
                        // single query
                        AlarmOccurrence(
                            alarm.name,
                            occurrence.measurementId
                        )
                    }
                }
                combine(flows) {
                    it.toList()
                }
            }
            .onEach { _allAlarmOccurrencesCache.value = it }
            .launchIn(scope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRecentAlarmOccurrences(userId: String) {
        recentAlarmOccurrencesJob?.cancel()
        recentAlarmOccurrencesJob = alarmOccurrenceRepository
            .getRecentAlarmOccurrencesForUser(userId)
            .flatMapLatest { occurrences ->
                val alarmIds = occurrences.map { it.alarmId }.distinct()

                if (alarmIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    alarmRepository.getAlarmsByIdList(alarmIds).map { alarms ->
                        // batch query, stored in a map
                        val alarmMap = alarms.associateBy { it.id }

                        occurrences.map { occ ->
                            RecentAlarmOccurrence(
                                alarmName = alarmMap[occ.alarmId]?.name ?: "Unknown alarm",
                                measurementId = occ.measurementId
                            )
                        }
                    }
                }
            }
            .onEach { _recentAlarmOccurrencesCache.value = it }
            .launchIn(scope)
    }
}
package mm.zamiec.garpom.domain.managers

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
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmOccurrenceRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmRepository
import mm.zamiec.garpom.ui.screens.alarms.AlarmOccurrenceItemUiState
import mm.zamiec.garpom.ui.screens.home.RecentAlarmOccurrenceItemUiState
import mm.zamiec.garpom.di.ApplicationScope
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AlarmOccurrencesListManager @Inject constructor (
    @param:ApplicationScope private val scope: CoroutineScope,
    authRepository: AuthRepository,

    private val alarmOccurrenceRepository: AlarmOccurrenceRepository,
    private val alarmRepository: AlarmRepository,
) {

    fun allAlarmOccurrences(): StateFlow<List<AlarmOccurrenceItemUiState>> = _allAlarmOccurrencesCacheItemUiState
    fun recentAlarmOccurrences(): StateFlow<List<RecentAlarmOccurrenceItemUiState>> = _recentAlarmOccurrencesCacheItemUiState

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


    private val _allAlarmOccurrencesCacheItemUiState = MutableStateFlow<List<AlarmOccurrenceItemUiState>>(emptyList())
    private var allAlarmOccurrencesJob: Job? = null

    private val _recentAlarmOccurrencesCacheItemUiState = MutableStateFlow<List<RecentAlarmOccurrenceItemUiState>>(emptyList())
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
                            return@mapLatest AlarmOccurrenceItemUiState("", occurrence.measurementId, LocalDateTime.ofInstant(occurrence.date, ZoneId.systemDefault()))
                        // single query
                        AlarmOccurrenceItemUiState(
                            alarm.name,
                            occurrence.measurementId,
                            date = LocalDateTime.ofInstant(occurrence.date, ZoneId.systemDefault())
                        )
                    }
                }
                combine(flows) {
                    it.toList()
                }
            }
            .onEach { _allAlarmOccurrencesCacheItemUiState.value = it }
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
                            RecentAlarmOccurrenceItemUiState(
                                alarmName = alarmMap[occ.alarmId]?.name ?: "Unknown alarm",
                                measurementId = occ.measurementId,
                                date = LocalDateTime.ofInstant(occ.date, ZoneId.systemDefault())
                            )
                        }
                    }
                }
            }
            .onEach { _recentAlarmOccurrencesCacheItemUiState.value = it }
            .launchIn(scope)
    }
}